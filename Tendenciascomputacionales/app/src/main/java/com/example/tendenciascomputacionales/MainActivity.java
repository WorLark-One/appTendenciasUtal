package com.example.tendenciascomputacionales;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    //variables
    private TextView grabar;
    private TextView temperatura;
    private TextView humedad;
    private Switch luz1;
    private Switch luz2;
    private Switch luz3;
    private Switch luz4;
    private Switch luz5;
    private Switch luz6;
    private Switch luz7;
    private Switch luz8;
    private Button btLuces;
    private TextView sensorLuminosidad;
    private TextView cerradura;
    private TextView personaPuerta;
    private Button abrirPuerta;

    public TextView estadoArduino;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    public static String address = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;
    public boolean activar;
    Handler bluetoothIn;
    final int handlerState = 0;
    private ConnectedThread MyConexionBT;
    Button btConectarArduino, btDesconectarArduino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //iniciar variables bt
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btConectarArduino = findViewById(R.id.btConectarArduino);
        btDesconectarArduino = findViewById(R.id.btDesconectarArduino);
        estadoArduino = (TextView) findViewById(R.id.estadoArduino);
        //iniciar variables
        this.grabar = (TextView) findViewById(R.id.txtGrabarVoz);
        this.temperatura = (TextView) findViewById(R.id.temperatura);
        this.humedad = (TextView) findViewById(R.id.humedad);
        this.luz1 = (Switch) findViewById(R.id.luz1);
        this.luz2 = (Switch) findViewById(R.id.luz2);
        this.luz3 = (Switch) findViewById(R.id.luz3);
        this.luz4 = (Switch) findViewById(R.id.luz4);
        this.luz5 = (Switch) findViewById(R.id.luz5);
        this.luz6 = (Switch) findViewById(R.id.luz6);
        this.luz7 = (Switch) findViewById(R.id.luz7);
        this.luz8 = (Switch) findViewById(R.id.luz8);
        this.btLuces = (Button) findViewById(R.id.btLuces);
        this.sensorLuminosidad = (TextView) findViewById(R.id.sensorLuminosidad);
        this.cerradura = (TextView) findViewById(R.id.cerradura);
        this.personaPuerta = (TextView) findViewById(R.id.personaPuerta);
        this.abrirPuerta = (Button) findViewById(R.id.abrirPuerta);

        this.btLuces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean [] luces = obtenerLucesEncendidas();
                MyConexionBT.write("a");
            }
        });

        this.abrirPuerta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //abrir puerta
            }
        });

        this.verificarEstadoBT();

        Set<BluetoothDevice> pairedDeveicesList = btAdapter.getBondedDevices();
        for(BluetoothDevice pairedDevice : pairedDeveicesList){
            if(pairedDevice.getName().equals("DESKTOP-C5RQ24Q")){
                address = pairedDevice.getAddress();
            }
        }

        btConectarArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activar = true;
                onResume();
            }
        });

        btDesconectarArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    btSocket.close();
                    estadoArduino.setText("Desconectado");
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private boolean[] obtenerLucesEncendidas (){
        boolean [] luces= new boolean[8];
        luces[0] = luz1.isChecked()? true : false;
        luces[1] = luz2.isChecked()? true : false;
        luces[2] = luz3.isChecked()? true : false;
        luces[3] = luz4.isChecked()? true : false;
        luces[4] = luz5.isChecked()? true : false;
        luces[5] = luz6.isChecked()? true : false;
        luces[6] = luz7.isChecked()? true : false;
        luces[7] = luz8.isChecked()? true : false;
        return luces;
    }

    private BluetoothSocket createBluetoothSocket (BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void verificarEstadoBT(){
        if(btAdapter.isEnabled()){

        }else{

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
        }

    }

    public void onResume() {
        super.onResume();


        if (activar) {
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);

            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
            }
            // Establece la conexión con el socket Bluetooth.
            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {

                }
            }
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();
            estadoArduino.setText("Conectado");
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {

                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    //grabar.setText(readMessage);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    //bluetoothIn.obtainMessage();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Envio de trama
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECOGNIZE_SPEECH_ACTIVITY:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> speech = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String strSpeech2Text = speech.get(0);
                    grabar.setText(strSpeech2Text);
                }
                break;
            default:
                break;
        }
    }
    public void onClickImgBtnHablar(View v) {
        Intent intentActionRecognizeSpeech = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Configura el Lenguaje (Español-México)
        intentActionRecognizeSpeech.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-MX");
        try {
            startActivityForResult(intentActionRecognizeSpeech,
                    RECOGNIZE_SPEECH_ACTIVITY);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Tú dispositivo no soporta el reconocimiento por voz",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
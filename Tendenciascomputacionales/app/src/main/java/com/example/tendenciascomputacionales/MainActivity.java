package com.example.tendenciascomputacionales;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.util.Log;
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
    private Button lucesNinguna;
    private Button lucesTodas;
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

    InputStream mmInStream;
    OutputStream mmOutStream;
    String msg;

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
        this.lucesNinguna = (Button) findViewById(R.id.lucesNinguna);
        this.lucesTodas = (Button) findViewById(R.id.lucesTodas);
        this.sensorLuminosidad = (TextView) findViewById(R.id.sensorLuminosidad);
        this.cerradura = (TextView) findViewById(R.id.cerradura);
        this.personaPuerta = (TextView) findViewById(R.id.personaPuerta);
        this.abrirPuerta = (Button) findViewById(R.id.abrirPuerta);

        this.btLuces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String luces = obtenerLucesEncendidas();
                MyConexionBT.write(luces);
            }
        });

        this.abrirPuerta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String abrirPuerta = "P1";
                cerradura.setText("Abierta");
                MyConexionBT.write(abrirPuerta);
            }
        });

        this.lucesNinguna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String luces = "L00000000";
                cambiarEstadoCheked(false);
                MyConexionBT.write(luces);
            }
        });

        this.lucesTodas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String luces = "L11111111";
                cambiarEstadoCheked(true);
                MyConexionBT.write(luces);
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

    private void cambiarEstadoCheked(boolean flag) {
        luz1.setChecked(flag);
        luz2.setChecked(flag);
        luz3.setChecked(flag);
        luz4.setChecked(flag);
        luz5.setChecked(flag);
        luz6.setChecked(flag);
        luz7.setChecked(flag);
        luz8.setChecked(flag);
    }

    private String obtenerLucesEncendidas (){
        String luces = "L";
        luces = luz1.isChecked()? luces+"1" : luces+"0";
        luces = luz2.isChecked()? luces+"1" : luces+"0";
        luces = luz3.isChecked()? luces+"1" : luces+"0";
        luces = luz4.isChecked()? luces+"1" : luces+"0";
        luces = luz5.isChecked()? luces+"1" : luces+"0";
        luces = luz6.isChecked()? luces+"1" : luces+"0";
        luces = luz7.isChecked()? luces+"1" : luces+"0";
        luces = luz8.isChecked()? luces+"1" : luces+"0";
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
        //private final InputStream mmInStream;
        //private final OutputStream mmOutStream;

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
            msg = "M";
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    if (readMessage.equals("I")) {
                        msg = msg + readMessage + ";";
                    } else if (readMessage.equals("T")) {
                        msg = msg + ";T";
                        //Log.d("mensajeRecibido", msg);
                        runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  Log.d("mensajeRecibido",msg);
                                  procesarStringRecibido(msg);
                                  msg = "M";
                              }
                          });
                        //procesarStringRecibido(msg);

                    } else {
                        msg = msg + readMessage;
                    }
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
                    procesarVoz(strSpeech2Text);
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

    private void procesarVoz(String comandoVoz){
        if (comandoVoz.equals("encender todas las luces") || comandoVoz.equals("Encender todas las luces")) {
            String luces = "L11111111";
            cambiarEstadoCheked(true);
            //Log.d("mensaje", "todas las luces");
            //MyConexionBT.write(luces);

        } else if(comandoVoz.equals("apagar todas las luces") || comandoVoz.equals("Apagar todas las luces")) {
            String luces = "L00000000";
            cambiarEstadoCheked(false);
            //Log.d("mensaje", "ninguna luz");
            //MyConexionBT.write(luces);
        } else if(comandoVoz.equals("abrir la puerta")) {
            String luces = "P1";
            cerradura.setText("Abierta");
            //Log.d("mensaje", "abrir la puerta");
            //MyConexionBT.write(luces);
        } else {
            int x = 0;
            String[] accionAux = comandoVoz.split(" ");
            String accion = accionAux[x];
            if (accion.equals("encender")) {
                String[] comandoAux = comandoVoz.split("encender la luz ");
                for (int i = 0; i < comandoAux.length; i++) {
                    String luces = comandoAux[i];
                    String[] lucesAux = luces.split(" ");
                    for (int j = 0; j < lucesAux.length; j++) {
                        String s = lucesAux[j];
                        cambiarSwitch(s,true);
                    }
                }
            } else if(accion.equals("apagar")){
                String[] comandoAux2 = comandoVoz.split("apagar la luz ");
                for (int i = 0; i < comandoAux2.length; i++) {
                    String luces = comandoAux2[i];
                    String[] lucesAux = luces.split(" ");
                    for (int j = 0; j < lucesAux.length; j++) {
                        String s = lucesAux[j];
                        cambiarSwitch(s,false);
                    }
                }
            }
            String luces = obtenerLucesEncendidas();
            //MyConexionBT.write(luces);
        }
    }

    private void cambiarSwitch(String luz, boolean flag) {
        if ( !luz.equals("y")) {
            switch (luz){
                case "1":
                case "uno":
                    luz1.setChecked(flag);
                    break;
                case "2":
                case "dos":
                    luz2.setChecked(flag);
                    break;
                case "3":
                case "tres":
                    luz3.setChecked(flag);
                    break;
                case "4":
                case "cuatro":
                    luz4.setChecked(flag);
                    break;
                case "5":
                case "cinco":
                    luz5.setChecked(flag);
                    break;
                case "6":
                case "seis":
                    luz6.setChecked(flag);
                    break;
                case "7":
                case "siete":
                    luz7.setChecked(flag);
                    break;
                case "8":
                case "ocho":
                    luz8.setChecked(flag);
                    break;
            }
        }
    }

    private void procesarStringRecibido(String mensaje) {
        String[] mensajeAux = mensaje.split(";");
        int inicio=0;
        int fin=6;
        if (mensajeAux.length == 7 && mensajeAux[inicio].equals("MI") && mensajeAux[fin].equals("T")){
            for (int i = 1; i < mensajeAux.length-1; i++) {
                switch (i){
                    case 1:
                        this.temperatura.setText(mensajeAux[i]);
                        break;
                    case 2:
                        this.humedad.setText(mensajeAux[i]);
                        break;
                    case 3:
                        this.sensorLuminosidad.setText(mensajeAux[i]);
                        break;
                    case 4:
                        String puerta = (mensajeAux[i].equals("1"))? "abierta" : "cerrada";
                        this.cerradura.setText(puerta);
                        break;
                    case 5:
                        String personaEnLaPuerta = (mensajeAux[i].equals("1"))? "Sí" : "No";
                        this.personaPuerta.setText(personaEnLaPuerta);
                        break;
                }
            }
        }
    }
}
package socketcalderon;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import com.temboo.Library.Parse.PushNotifications.SendNotification;
import com.temboo.Library.Parse.PushNotifications.SendNotification.SendNotificationInputSet;
import com.temboo.Library.Parse.PushNotifications.SendNotification.SendNotificationResultSet;
import com.temboo.core.TembooException;
import com.temboo.core.TembooSession;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SocketGrafico {
	
    static float TempUmbHigh;
    static float TempUmbLow;

//    static double LimInfTemp = 16.9; // " 16.9"  'Set point Inferior de temperatura para el margen establecido de operacion
//    static double LimSupTemp = 27.1;  //" 27.1"  'Set point Superior de temperatura para el margen establecido de operacion
//    static double LimInfVolt = 79.9;  //" 79.9"  'Set point Inferior de Voltaje Rms para el margen establecido de operacion
//    static double LimSupVolt = 150.0;  //"150.0"  'Set point Superior de Voltaje Rms para el margen establecido de operacion
//    static double LimInfAmps = 0.01;//  " 0.01"  'Set point Inferior de Corriente Rms para el margen establecido de operacion
//    static double LimSupAmps = 24.99; //  "24.99"  'Set point Superior de Corriente Rms para el margen establecido de operacion
//    static double LimInfHum = 24.9; //   " 24.9"  'Set point Inferior de Humedad Rms para el margen establecido de operacion
//    static double LimSupHum = 70.0;//  " 70.0"  'Set point Superior de Humedad Rms para el margen establecido de operacion
    
    static double LimInfTemp = 16.9; // " 16.9"  'Set point Inferior de temperatura para el margen establecido de operacion
    static double TempEstab=20;
    static double LimSupTemp = 27.1;  //" 27.1"  'Set point Superior de temperatura para el margen establecido de operacion
    static double LimInfVolt = 79.9;  //" 79.9"  'Set point Inferior de Voltaje Rms para el margen establecido de operacion
    static double VoltEstab=120;
    static double LimSupVolt = 150.0;  //"150.0"  'Set point Superior de Voltaje Rms para el margen establecido de operacion
    static double LimInfAmps = 0.01;//  " 0.01"  'Set point Inferior de Corriente Rms para el margen establecido de operacion
    static double AmpsEstab=20;
    static double LimSupAmps = 24.99; //  "24.99"  'Set point Superior de Corriente Rms para el margen establecido de operacion
    static double LimInfHum = 24.9; //   " 24.9"  'Set point Inferior de Humedad Rms para el margen establecido de operacion
    static double HumEstab=35;
    static double LimSupHum = 70.0;//  " 70.0"  'Set point Superior de Humedad Rms para el margen establecido de operacion
    
    static boolean alertaTemp=false;
    static boolean alertaHumedad=false;
    static boolean alertaAmperaje=false;
    static boolean alertaVoltaje=false;
    
    static boolean estableTemp=true;
    static boolean estableHumedad=true;
    static boolean estableAmperaje=true;
    static boolean estabkeVoltaje=true;
    
    static boolean supervisarTemp=false;
    static boolean supervisarHumedad=false;
    static boolean supervisarAmperaje=false;
    static boolean supervisarVoltaje=false;
    
    static Socket clientSocket;
    static String ip="";
    static int puerto=0;

    static int Temperatura = 5;
    static double Voltage = 150;

	private JFrame frame;
	
	static JTextArea tatrama = new JTextArea();
	private JTextField tfTempMin;
	private JTextField tfTempEs;
	private JTextField tfTemMax;
	private JTextField tfVoltMin;
	private JTextField tfVoltEs;
	private JTextField tfVoltMax;
	private JTextField tfAmperMin;
	private JTextField tfAmperEs;
	private JTextField tfAmperMax;
	private JTextField tfHumeMin;
	private JTextField tfHumeEs;
	private JTextField tfHumeMax;
	private JTextField tfIp;
	private JTextField tfPuerto;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SocketGrafico window = new SocketGrafico();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}
	
	public void esperar(int segundos) {
        try {
            Thread.sleep(segundos * 1000);
        } catch (Exception e) {
// Mensaje en caso de que falle
        }
    }

    static void usingCharArray() throws TembooException {
        Stopwatch sw = new Stopwatch();
        try {
            boolean init = true;
            clientSocket = new Socket(ip,puerto);
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            char[] currentFrame = new char[113];
            char[] T8c1 = new char[72];
            char[] T8c2 = new char[113];
            
            int read;
            sw.start();
            read = br.read(currentFrame);
            while (read != -1) {
                if (!init) {
                    sw.start();
                    read = br.read(currentFrame);
                }
                //String output = new String(buffer, 0, read);
                if (currentFrame[2] == 'c') {
                    if (currentFrame[3] == '1') {

                        T8c1 = Arrays.copyOfRange(currentFrame, 11, 71);

                    } else if (currentFrame[3] == '2') {

                        T8c2 = Arrays.copyOfRange(currentFrame, 12, 112);

                        List<String> allMatches = new ArrayList<String>();
                        Matcher m = Pattern.compile("[[0-9]+[.]?[\\s]?]{5}")
                                .matcher(new String(T8c2));

                        while (m.find()) {
                            allMatches.add(m.group());

                        }

                        //Trama8c2 trama = new Trama8c2();
                        Trama8c2 trama = Data.trama8c2;

                        trama.setVolts_Fase_R(Double.parseDouble(allMatches.get(0)));

                        trama.setVolts_Fase_S(Double.parseDouble(allMatches.get(1)));
                        trama.setVolts_Fase_T(Double.parseDouble(allMatches.get(2)));
                        trama.setUPS_Amps_Fase_1(Double.parseDouble(allMatches.get(3)));

                        trama.setAmps_Fase_R(Double.parseDouble(allMatches.get(4)));
                        trama.setAmps_Fase_S(Double.parseDouble(allMatches.get(5)));
                        trama.setAmps_Fase_T(Double.parseDouble(allMatches.get(6)));
                        trama.setUPS_Amps_Fase_2(Double.parseDouble(allMatches.get(7)));

                        trama.setTemp_Pasillo_Frio(Double.parseDouble(allMatches.get(8)));
                        trama.setHumd_Pasillo_Frio(Double.parseDouble(allMatches.get(9)));
                        trama.setTemp_Pasillo_Caliente(Double.parseDouble(allMatches.get(10)));
                        trama.setHumd_Pasillo_Caliente(Double.parseDouble(allMatches.get(11)));

                        trama.setVolts_PDU_F1_GAB1(Double.parseDouble(allMatches.get(12)));
                        trama.setAmps_PDU_F1_GAB1(Double.parseDouble(allMatches.get(13)));
                        trama.setInlet_Temp_GAB1(Double.parseDouble(allMatches.get(14)));
                        trama.setVolts_PDU_F2_GAB1(Double.parseDouble(allMatches.get(15)));

                        trama.setVolts_PDU_F1_GAB2(Double.parseDouble(allMatches.get(16)));
                        trama.setAmps_PDU_F1_GAB2(Double.parseDouble(allMatches.get(17)));
                        trama.setInlet_Temp_GAB2(Double.parseDouble(allMatches.get(18)));
                        trama.setVolts_PDU_F2_GAB2(Double.parseDouble(allMatches.get(19)));

                        //********************************************************************************************************************************************************
                        runpush();
                         //muestraContenido("C:\\Users\\cristian\\Desktop\\Proyecto\\Archivos Compartidos\\XPORT/Alerta-43A.txt");
                        
                        //*********************************************************************************************************************************************************************************************                       
                    } else if (currentFrame[3] == '3') {

                    }
                } else {

                }
               // System.out.println("oper time "+sw.elapsedTime()+ " ms");

                /// System.out.println(new String(T8c1));
                System.out.println("oper time " + sw.elapsedTime() + " ms");

                System.out.println("Trama 8C2 : " + new String(T8c2));
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                
                tatrama.setText(df.format(date)+": Trama 8C2");
                //tatrama.setCaretPosition(tatrama.getDocument().getLength());
                System.out.flush();
                init = false;

            };
            clientSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(SocketCalderon.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    //////////////////////////////////////////
    public static void muestraContenido(String archivo) throws FileNotFoundException, IOException {
        String cadena;
        FileReader f = new FileReader(archivo);
        BufferedReader b = new BufferedReader(f);
        while((cadena = b.readLine())!=null) {
            System.out.println(cadena);
        }
        b.close();
    }
    
    
    //////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runpush() throws TembooException, IOException {
        Trama8c2 trama = Data.trama8c2;
        
       // muestraContenido("C:\\Users\\cristian\\Desktop\\Proyecto\\Archivos Compartidos\\XPORT/Alerta-43A.txt");
        
        String [] mensaje = new String[10];
        mensaje[0] = "El sistema ha detectado un problema relacionado con la temperatura de operacion en gabinete 1 del datacenter GDCIM ubicado en este sitio.\\n"
                + "\\nLA TEMPERATURA SE ENCUENTRA POR DEBAJO DEL MARGEN ESTABLECIDO DE OPERACION.";
        mensaje[1] = "El sistema ha detectado un problema relacionado con la temperatura de operacion en gabinete 1 del datacenter GDCIM ubicado en este sitio. \\n"
                + "\\nLA TEMPERATURA SE ENCUENTRA POR ENCIMA DEL MARGEN ESTABLECIDO DE OPERACION.";
        mensaje[2] = "El sistema ha detectado un problema relacionado con el Voltaje en la PDU-Fase 1 Gabinete 1 del datacenter ubicado en este sitio: Datacenter Bucaramanga.\\n"
                + "\\nEL VOLTAJE SE ENCUENTRA POR DEBAJO DEL MARGEN ESTABLECIDO DE OPERACIÓN.";
        mensaje[3] = "El sistema ha detectado un problema relacionado con el Voltaje en la PDU-Fase 1 Gabinete 1 del datacenter ubicado en este sitio: Datacenter Bucaramanga.\\n"
                + "\\nEL VOLTAJE SE ENCUENTRA POR ENCIMA DEL MARGEN ESTABLECIDO DE OPERACION.";
        mensaje[4] = "El sistema ha detectado un problema relacionado con el Voltaje en la PDU-Fase 2 Gabinete 1 del datacenter ubicado en este sitio: Datacenter Bucaramanga.\\n"
                + "\\nEL VOLTAJE SE ENCUENTRA POR DEBAJO DEL MARGEN ESTABLECIDO DE OPERACION.";
        mensaje[5] = "El sistema ha detectado un problema relacionado con el Voltaje en la PDU-Fase 2 Gabinete 1 del datacenter ubicado en este sitio: Datacenter Bucaramanga.\\n"
                + "\\nEL VOLTAJE SE ENCUENTRA POR ENCIMA DEL MARGEN ESTABLECIDO DE OPERACION.";
        mensaje[6] = "El sistema ha detectado un problema relacionado con la Corriente de operacion en la PDU-Fase 1 Gabinete 1  del datacenter ubicado en este sitio: Datacenter Bucaramanga.\\n"
                + "\\nLA CORRIENTE SE ENCUENTRA POR DEBAJO DEL MARGEN ESTABLECIDO DE OPERACION.";
        mensaje[7] = "El sistema ha detectado un problema relacionado con la Corriente de operacion en la PDU-Fase 1 Gabinete 1  del datacenter ubicado en este sitio: Datacenter Bucaramanga.\\n"
                + "\\nLA CORRIENTE SE ENCUENTRA POR ENCIMA DEL MARGEN ESTABLECIDO DE OPERACION.";
        
                         //Codigo envia notificacion push

                        // Instantiate the choreography, using a previously instantiated
        // TembooSession object, eg:
        TembooSession session = new TembooSession("petux", "GDCIM", "fstmdxUN0wbQpLpC3baqSDVlwj0JNOCl");
        SendNotification sendNotificationChoreo = new SendNotification(session);
        // Get an InputSet object for the choreo
        SendNotificationInputSet sendNotificationInputs = sendNotificationChoreo.newInputSet();
        SendNotificationInputSet sendNotificationInputs1 = sendNotificationChoreo.newInputSet();

        // Set credential to use for execution
        sendNotificationInputs.setCredential("JavaGdcim");

        boolean contador = false;
        boolean contador2 = false;

       
       
     // Set inputs
        if (trama.getInlet_Temp_GAB1 () 
            <= LimInfTemp) {

                sendNotificationInputs.set_Notification(
                    "{\"channel\": \"Temperatura\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[0] + "\\n\\nTEMPERATURA INFERIOR A 17  Celsius : " + trama.getInlet_Temp_GAB1() + " C\"}}");
            System.out.println("El valor de temperatura es " + trama.getInlet_Temp_GAB1());
            if (!alertaTemp && supervisarTemp) {
                alertaTemp = true;
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
            }

        }

        if (trama.getInlet_Temp_GAB1 () 
            >= LimSupTemp) {

                sendNotificationInputs.set_Notification(
                    "{\"channel\": \"Temperatura\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[1] + "\\n\\nTEMPERATURA SUPERIOR A 27 Celsius :" + trama.getInlet_Temp_GAB1() + " C\"}}");
            System.out.println("El valor de temperatura es " + trama.getInlet_Temp_GAB1());
            if (!alertaTemp && supervisarTemp) {
                alertaTemp = true;
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
            }

        }

        
            else {
                System.out.println("No llego nada de temperatura");
        }
        if (contador

        
            == true) {
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
        }

        if (trama.getVolts_PDU_F1_GAB1 () 
            <= LimInfVolt) {
            	   sendNotificationInputs.set_Notification(
                    "{\"channel\": \"Voltage\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[2] + "\\n\\nVoltaje INFERIOR A 80 Volts RMS : " + trama.getVolts_PDU_F1_GAB1() + " V\"}}");
            System.out.println("El valor de voltage es " + trama.getVolts_PDU_F1_GAB1());
            if (!alertaVoltaje && supervisarVoltaje) {
                alertaVoltaje = true;
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
            }
        }

        if (trama.getVolts_PDU_F1_GAB1 () 
            >= LimSupVolt) {
         	   sendNotificationInputs.set_Notification(
                    "{\"channel\": \"Voltage\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[3] + "\\n\\nVoltaje SUPERIOR A 150 Volts RMS : " + trama.getVolts_PDU_F1_GAB1() + " V\"}}");
            System.out.println("El valor de voltage es " + trama.getAmps_PDU_F1_GAB1());
            if (!alertaVoltaje && supervisarVoltaje) {
                alertaVoltaje = true;
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
            }
        }

        if (trama.getVolts_PDU_F2_GAB1 () 
            <= LimInfVolt) {
         	   sendNotificationInputs.set_Notification(
                    "{\"channel\": \"Voltage\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[4] + "\\n\\nVoltaje INFERIOR A 80 Volts RMS : " + trama.getVolts_PDU_F2_GAB1() + " V\"}}");
            System.out.println("El valor de voltage es " + trama.getVolts_PDU_F2_GAB1());
            if (!alertaVoltaje && supervisarVoltaje) {
                alertaVoltaje = true;
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
            }
        }

        if (trama.getVolts_PDU_F2_GAB1() 
            >= LimSupVolt) {
      	   sendNotificationInputs.set_Notification(
                    "{\"channel\": \"Voltage\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[5] + "\\n\\nVoltaje SUPERIOR A 150 Volts RMS : " + trama.getVolts_PDU_F2_GAB1() + " V\"}}");
            System.out.println("El valor de voltage es " + trama.getVolts_PDU_F2_GAB1());
            if (!alertaVoltaje && supervisarVoltaje) {
                alertaVoltaje = true;
                SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
            }
        }
        
        if (trama.getAmps_PDU_F1_GAB1() <= LimInfAmps) {
             	   sendNotificationInputs.set_Notification(
                        "{\"channel\": \"Everyone\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[6] + "\\n\\nCORRIENTE INFERIOR A 0.1 AMPERS : " + trama.getAmps_PDU_F1_GAB1() + " AMPS\"}}");
                System.out.println("El valor de amperage es " + trama.getAmps_PDU_F1_GAB1());
                if (!alertaAmperaje && supervisarAmperaje) {
                    alertaAmperaje = true;
                    SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
                }
            }

            if (trama.getAmps_PDU_F1_GAB1() >= LimSupAmps) {
          	   sendNotificationInputs.set_Notification(
                        "{\"channel\": \"Gdcim\", \"type\": \"android\", \"data\": {\"alert\": \"" + mensaje[7] + "\\n\\nCorriente SUPERIOR A 25 Ampers : " + trama.getAmps_PDU_F1_GAB1() + " AMPS\"}}");
                System.out.println("El valor de amperage es " + trama.getAmps_PDU_F1_GAB1());
                if (!alertaAmperaje && supervisarAmperaje) {
                    alertaAmperaje = true;
                    SendNotificationResultSet sendNotificationResults = sendNotificationChoreo.execute(sendNotificationInputs);
                }
            }
        
        
        
    }
    
    
        // Execute Choreo
        

    static void usingString() {
        Stopwatch sw = new Stopwatch();
        try {
            boolean init = true;
            Socket clientSocket = new Socket("190.90.244.51", 3307);
            InputStream is = clientSocket.getInputStream();
            byte[] buffer = new byte[2048];
            int read;
            sw.start();
            read = is.read(buffer);
            while (read != -1) {
                if (!init) {
                    sw.start();
                    read = is.read(buffer);
                }
                String output = new String(buffer, 0, read);
                System.out.println("oper time " + sw.elapsedTime() + " ms");

                System.out.println(output);
                System.out.flush();
                init = false;

            };
            clientSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(SocketCalderon.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }


	/**
	 * Create the application.
	 */
	public SocketGrafico() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 399);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JCheckBox chkTemp = new JCheckBox("Temperatura");
		
		chkTemp.setBounds(28, 102, 100, 23);
		frame.getContentPane().add(chkTemp);
		tatrama.setFont(new Font("Arial Narrow", Font.PLAIN, 10));
		tatrama.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent arg0) {
			}
		});
		tatrama.setEditable(false);
		
		
		
		//tatrama.setAutoscrolls(true);
		//frame.getContentPane().add(tatrama);
		JScrollPane scroll=new JScrollPane(tatrama);
		scroll.setBounds(100, 60, 100, 20);
		frame.getContentPane().add(scroll);
		
		JCheckBox chkHume = new JCheckBox("Humedad");
		chkHume.setBounds(28, 186, 100, 23);
		frame.getContentPane().add(chkHume);
		
		JCheckBox chkVolt = new JCheckBox("Voltaje");
		chkVolt.setBounds(28, 130, 100, 23);
		frame.getContentPane().add(chkVolt);
		
		JCheckBox chkAmper = new JCheckBox("Amperaje");
		chkAmper.setBounds(28, 158, 100, 23);
		frame.getContentPane().add(chkAmper);
		
		JButton btnConectar = new JButton("Conectar");
		btnConectar.setBounds(340, 238, 89, 23);
		frame.getContentPane().add(btnConectar);
		
		JLabel lblRecibido = new JLabel("Recibido");
		lblRecibido.setBounds(28, 62, 72, 14);
		frame.getContentPane().add(lblRecibido);
		
		JLabel lblMinima = new JLabel("Minima");
		lblMinima.setBounds(154, 106, 46, 14);
		frame.getContentPane().add(lblMinima);
		
		tfTempMin = new JTextField();
		tfTempMin.setText("16.9");
		tfTempMin.setEditable(false);
		tfTempMin.setBounds(199, 103, 86, 20);
		frame.getContentPane().add(tfTempMin);
		tfTempMin.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Estable");
		lblNewLabel.setBounds(295, 106, 46, 14);
		frame.getContentPane().add(lblNewLabel);
		
		tfTempEs = new JTextField();
		tfTempEs.setText("20");
		tfTempEs.setEditable(false);
		tfTempEs.setBounds(343, 103, 86, 20);
		frame.getContentPane().add(tfTempEs);
		tfTempEs.setColumns(10);
		
		JLabel lblMaxima = new JLabel("Maxima");
		lblMaxima.setBounds(439, 106, 46, 14);
		frame.getContentPane().add(lblMaxima);
		
		tfTemMax = new JTextField();
		tfTemMax.setText("27.1");
		tfTemMax.setEditable(false);
		tfTemMax.setBounds(495, 103, 86, 20);
		frame.getContentPane().add(tfTemMax);
		tfTemMax.setColumns(10);
		
		JLabel label = new JLabel("Minima");
		label.setBounds(154, 134, 46, 14);
		frame.getContentPane().add(label);
		
		tfVoltMin = new JTextField();
		tfVoltMin.setText("79.9");
		tfVoltMin.setEditable(false);
		tfVoltMin.setColumns(10);
		tfVoltMin.setBounds(199, 131, 86, 20);
		frame.getContentPane().add(tfVoltMin);
		
		JLabel label_1 = new JLabel("Estable");
		label_1.setBounds(295, 134, 46, 14);
		frame.getContentPane().add(label_1);
		
		tfVoltEs = new JTextField();
		tfVoltEs.setText("119");
		tfVoltEs.setEditable(false);
		tfVoltEs.setColumns(10);
		tfVoltEs.setBounds(343, 131, 86, 20);
		frame.getContentPane().add(tfVoltEs);
		
		JLabel label_2 = new JLabel("Maxima");
		label_2.setBounds(439, 134, 46, 14);
		frame.getContentPane().add(label_2);
		
		tfVoltMax = new JTextField();
		tfVoltMax.setText("150");
		tfVoltMax.setEditable(false);
		tfVoltMax.setColumns(10);
		tfVoltMax.setBounds(495, 131, 86, 20);
		frame.getContentPane().add(tfVoltMax);
		
		JLabel label_3 = new JLabel("Minima");
		label_3.setBounds(154, 163, 46, 14);
		frame.getContentPane().add(label_3);
		
		tfAmperMin = new JTextField();
		tfAmperMin.setText("0.01");
		tfAmperMin.setEditable(false);
		tfAmperMin.setColumns(10);
		tfAmperMin.setBounds(199, 160, 86, 20);
		frame.getContentPane().add(tfAmperMin);
		
		JLabel label_4 = new JLabel("Estable");
		label_4.setBounds(295, 163, 46, 14);
		frame.getContentPane().add(label_4);
		
		tfAmperEs = new JTextField();
		tfAmperEs.setText("20");
		tfAmperEs.setEditable(false);
		tfAmperEs.setColumns(10);
		tfAmperEs.setBounds(343, 160, 86, 20);
		frame.getContentPane().add(tfAmperEs);
		
		JLabel label_5 = new JLabel("Maxima");
		label_5.setBounds(439, 163, 46, 14);
		frame.getContentPane().add(label_5);
		
		tfAmperMax = new JTextField();
		tfAmperMax.setText("24.99");
		tfAmperMax.setEditable(false);
		tfAmperMax.setColumns(10);
		tfAmperMax.setBounds(495, 160, 86, 20);
		frame.getContentPane().add(tfAmperMax);
		
		JLabel label_6 = new JLabel("Minima");
		label_6.setBounds(154, 192, 46, 14);
		frame.getContentPane().add(label_6);
		
		tfHumeMin = new JTextField();
		tfHumeMin.setText("24.9");
		tfHumeMin.setEditable(false);
		tfHumeMin.setColumns(10);
		tfHumeMin.setBounds(199, 189, 86, 20);
		frame.getContentPane().add(tfHumeMin);
		
		JLabel label_7 = new JLabel("Estable");
		label_7.setBounds(295, 192, 46, 14);
		frame.getContentPane().add(label_7);
		
		tfHumeEs = new JTextField();
		tfHumeEs.setText("35");
		tfHumeEs.setEditable(false);
		tfHumeEs.setColumns(10);
		tfHumeEs.setBounds(343, 189, 86, 20);
		frame.getContentPane().add(tfHumeEs);
		
		JLabel label_8 = new JLabel("Maxima");
		label_8.setBounds(439, 192, 46, 14);
		frame.getContentPane().add(label_8);
		
		tfHumeMax = new JTextField();
		tfHumeMax.setText("70");
		tfHumeMax.setEditable(false);
		tfHumeMax.setColumns(10);
		tfHumeMax.setBounds(495, 189, 86, 20);
		frame.getContentPane().add(tfHumeMax);
		
		JLabel lblIp = new JLabel("IP");
		lblIp.setBounds(28, 242, 46, 14);
		frame.getContentPane().add(lblIp);
		
		tfIp = new JTextField();
		tfIp.setText("138.186.191.23");
		tfIp.setBounds(56, 239, 86, 20);
		frame.getContentPane().add(tfIp);
		tfIp.setColumns(10);
		
		JLabel lblPuerto = new JLabel("Puerto");
		lblPuerto.setBounds(154, 242, 46, 14);
		frame.getContentPane().add(lblPuerto);
		
		tfPuerto = new JTextField();
		tfPuerto.setText("3307");
		tfPuerto.setBounds(199, 239, 86, 20);
		frame.getContentPane().add(tfPuerto);
		tfPuerto.setColumns(10);
		
		chkTemp.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				if (chkTemp.isSelected()) {
					tfTemMax.setEditable(true);
					tfTempEs.setEditable(true);
					tfTempMin.setEditable(true);
					supervisarTemp=true;
					
				}
				else {
					tfTemMax.setEditable(false);
					tfTempEs.setEditable(false);
					tfTempMin.setEditable(false);
					supervisarTemp=false;
				}
				
			}
		});
		
		chkVolt.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				
				if (chkVolt.isSelected()) {
					tfVoltEs.setEditable(true);
					tfVoltMax.setEditable(true);
					tfVoltMin.setEditable(true);
					supervisarVoltaje=true;
					
				}
				else {
					tfVoltEs.setEditable(false);
					tfVoltMax.setEditable(false);
					tfVoltMin.setEditable(false);
					supervisarVoltaje=false;
				}
				
			}
		});
		
		chkAmper.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				if (chkAmper.isSelected()) {
					tfAmperEs.setEditable(true);
					tfAmperMax.setEditable(true);
					tfAmperMin.setEditable(true);
					supervisarAmperaje=true;
					
					
				}
				else {
					tfAmperEs.setEditable(false);
					tfAmperMax.setEditable(false);
					tfAmperMin.setEditable(false);
					supervisarAmperaje=false;
				}
				
			}
		});
		
		
		chkHume.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				if (chkHume.isSelected()) {
					tfHumeEs.setEditable(true);
					tfHumeMax.setEditable(true);
					tfHumeMin.setEditable(true);
					supervisarHumedad=true;
					
				}
				else {
					tfHumeEs.setEditable(false);
					tfHumeMax.setEditable(false);
					tfHumeMin.setEditable(false);
					supervisarHumedad=false;
				}
				
			}
		});
		
		btnConectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if (tfIp.getText().equals("") || tfPuerto.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Debe ingresar una direccion ip y un puerto");
				}
				else {
				
				if (!chkAmper.isSelected()&&!chkHume.isSelected()&&!chkTemp.isSelected()&&!chkVolt.isSelected()) {
					JOptionPane.showMessageDialog(null, "No ha seleccionado ningun control");
				}
				else {
				
					if (chkAmper.isSelected()) {
						LimInfAmps=Double.parseDouble(tfAmperMin.getText());
						LimSupAmps=Double.parseDouble(tfAmperMax.getText());
						AmpsEstab=Double.parseDouble(tfAmperEs.getText());
						
					}
					if (chkHume.isSelected()) {
						
						LimInfHum=Double.parseDouble(tfHumeMin.getText());
						LimSupHum=Double.parseDouble(tfHumeMax.getText());
						HumEstab=Double.parseDouble(tfHumeEs.getText());
						
					}
					if (chkTemp.isSelected()) {
						LimInfTemp=Double.parseDouble(tfTempMin.getText());
						LimSupTemp=Double.parseDouble(tfTemMax.getText());
						TempEstab=Double.parseDouble(tfTempEs.getText());
					}
					if (chkVolt.isSelected()) {
						LimInfVolt=Double.parseDouble(tfVoltMin.getText());
						LimSupVolt=Double.parseDouble(tfVoltMax.getText());
						VoltEstab=Double.parseDouble(tfVoltEs.getText());
					}
					ip=tfIp.getText();
					puerto=Integer.parseInt(tfPuerto.getText());
					
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								usingCharArray();
							} catch (TembooException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					t.start();
					btnConectar.setEnabled(false);
					//btnDesconectar.setEnabled(true);
				}
			}
				
				
			}
		});
		
	}
}

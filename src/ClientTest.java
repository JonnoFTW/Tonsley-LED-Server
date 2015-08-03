import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.GridLayout;



public class ClientTest {
	private JTextField txtLocalhost;

	public ClientTest() {
		final JFrame frame = new JFrame("Client Test");
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JTextArea textArea = new JTextArea();
		textArea.setText("8\n8\n0\n0,1,0-0,0,1-1,1,1\n");
		frame.getContentPane().add(textArea,BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(50, 90));
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(2, 1, 5, 5));
		final JButton button = new JButton("Send!");
		panel.add(button);
		txtLocalhost = new JTextField();
		txtLocalhost.setText("localhost:7778");
		panel.add(txtLocalhost);
		txtLocalhost.setColumns(10);
		
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ev) {
				(new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							String[] pieces = txtLocalhost.getText().split(":");
							Socket outSocket = new Socket(pieces[0],Integer.parseInt(pieces[1]));
							PrintWriter out =new PrintWriter(outSocket.getOutputStream());
							out.write(textArea.getText());
							out.flush();
							out.close();
							outSocket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				})).start();
			}
		});
		
		
	}

	public static void main(String[] args) {
		new ClientTest();
	}
}

package xlz.launcher;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.apache.commons.io.FileUtils;

import xlz.launcher.utils.Unzip;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		JFrame frame = new JFrame("Smoke Launcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(350, 100);

		JLabel label = new JLabel("Smoke Launcher");
		label.setToolTipText("Status of Smoke");

		JTextField text = new JTextField("Username");
		text.setToolTipText("The username to use when playing Minecraft");
		text.setPreferredSize(new Dimension(150, 30));
		((AbstractDocument) text.getDocument()).setDocumentFilter(new DocumentFilter() {
			int maxChars = 16;

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				int currentLength = fb.getDocument().getLength();
				int difference = maxChars - currentLength + length;

				if (difference > 0) {
					super.replace(fb, offset, length, text.substring(0, Math.min(difference, text.length())), attrs);
				}
			}
		});

		JButton button = new JButton("Launch");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchSmoke(text.getText());
			}
		});
		button.setToolTipText("To launch Smoke Client");

		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(text);
		panel.add(button);

		frame.add(panel);
		frame.setVisible(true);
	}

	public static void launchSmoke(String username) {
		File mcDir = new File(System.getProperty("user.dir"));
		File mcAssets = new File(mcDir.toString() + "assets");

		File natives = new File(System.getProperty("user.dir") + File.separator + "natives.zip");
		File libraries = new File(System.getProperty("user.dir") + File.separator + "libraries.zip");
		File jar = new File(System.getProperty("user.dir") + File.separator + "Smoke.jar");

		try {
			FileUtils.copyURLToFile(
					new URL("https://github.com/xlzxq/SmokeLauncher/blob/main/smokeAssets/natives.zip?raw=true"),
					natives);
			FileUtils.copyURLToFile(
					new URL("https://github.com/xlzxq/SmokeLauncher/blob/main/smokeAssets/libraries.zip?raw=true"),
					libraries);
			FileUtils.copyURLToFile(
					new URL("https://github.com/xlzxq/SmokeLauncher/blob/main/smokeAssets/Smoke.jar?raw=true"), jar);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Unzip unzipper = new Unzip();

		try {
			unzipper.unzip(natives.toString(), System.getProperty("user.dir") + File.separator + "natives");
			natives.delete();

			unzipper.unzip(libraries.toString(), System.getProperty("user.dir") + File.separator + "libraries");
			libraries.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Process process = Runtime.getRuntime().exec("java -" + "Xms1024M " + "-Xmx4096M " + "-Djava.library.path=\""
					+ System.getProperty("user.dir") + File.separator + "natives" + "\" " + "-cp \""
					+ System.getProperty("user.dir") + File.separator + "libraries" + File.separator + "*" + ";"
					+ jar.toString() + "\" " + "net.minecraft.client.main.Main " + "--width 854 " + "--height 480 "
					+ "--username " + username + " " + "--version 1.8.9 " + "--gameDir " + mcDir.toString() + " "
					+ "--assetsDir " + mcAssets.toString() + " " + "--assetIndex 1.8.9 " + "--uuid N/A "
					+ "--accessToken aeef7bc935f9420eb6314dea7ad7e1e5 " + "--userType mojang");

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String s = null;
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

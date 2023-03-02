package xlz.launcher;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.apache.commons.io.FileUtils;

import com.sun.management.OperatingSystemMXBean;

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
		frame.setSize(450, 135);

		JLabel title = new JLabel("Smoke Launcher"), currRam = new JLabel("2048mb");
		currRam.setToolTipText("The amount of ram Smoke is allowed to use");

		JTextField username = new JTextField("Username");
		username.setToolTipText("The username to use when playing Minecraft");
		username.setPreferredSize(new Dimension(150, 30));
		((AbstractDocument) username.getDocument()).setDocumentFilter(new DocumentFilter() {
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

		JCheckBox manualInstall = new JCheckBox("Run without installing", true);
		manualInstall.setToolTipText("Skips the installing part (use if natives, libs and jar file are in place)");

		JSlider ramUsage = new JSlider(1024,
				(int) ((((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
						.getTotalPhysicalMemorySize() / 1000) / 1000));
		ramUsage.setValue(2048);
		ramUsage.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				currRam.setText(ramUsage.getValue() + "mb");
			}
		});

		JButton button = new JButton("Launch");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchSmoke(username.getText(), ramUsage.getValue(), manualInstall.isSelected());
			}
		});
		button.setToolTipText("To launch Smoke Client (this will freeze launcher while ingame)");

		JPanel main = new JPanel(), settings = new JPanel();
		main.add(title);
		main.add(button);
		settings.add(username);
		settings.add(ramUsage);
		settings.add(currRam);
		settings.add(manualInstall);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main", main);
		tabbedPane.addTab("Settings", settings);

		frame.getContentPane().add(tabbedPane);
		frame.setVisible(true);
	}

	public static void launchSmoke(String username, int ram, boolean manuallyInstalled) {
		File mcDir = new File(System.getProperty("user.dir"));
		File mcAssets = new File(mcDir.toString() + "assets");

		File natives = new File(System.getProperty("user.dir") + File.separator + "natives.zip");
		File libraries = new File(System.getProperty("user.dir") + File.separator + "libraries.zip");
		File jar = new File(System.getProperty("user.dir") + File.separator + "Smoke.jar");

		if (!manuallyInstalled) {
			try {
				FileUtils.copyURLToFile(
						new URL("https://github.com/xlzxq/SmokeLauncher/blob/main/smokeAssets/natives.zip?raw=true"),
						natives);
				FileUtils.copyURLToFile(
						new URL("https://github.com/xlzxq/SmokeLauncher/blob/main/smokeAssets/libraries.zip?raw=true"),
						libraries);
				FileUtils.copyURLToFile(
						new URL("https://github.com/xlzxq/SmokeLauncher/blob/main/smokeAssets/Smoke.jar?raw=true"),
						jar);
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
		}

		try {
			Process process = Runtime.getRuntime().exec("java -" + "Xms1024M " + "-Xmx" + ram + "M "
					+ "-Djava.library.path=\"" + System.getProperty("user.dir") + File.separator + "natives" + "\" "
					+ "-cp \"" + System.getProperty("user.dir") + File.separator + "libraries" + File.separator + "*"
					+ ";" + jar.toString() + "\" " + "net.minecraft.client.main.Main " + "--width 854 "
					+ "--height 480 " + "--username " + username + " " + "--version 1.8.9 " + "--gameDir "
					+ mcDir.toString() + " " + "--assetsDir " + mcAssets.toString() + " " + "--assetIndex 1.8.9 "
					+ "--uuid N/A " + "--accessToken aeef7bc935f9420eb6314dea7ad7e1e5 " + "--userType mojang");

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

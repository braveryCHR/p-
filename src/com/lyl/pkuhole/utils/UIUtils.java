package com.lyl.pkuhole.utils;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

public class UIUtils {

	public static final Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

	public static final int MAX_IMAGE_HEIGHT = 800;

	public static void setDefaultLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public static void initGlobalFont() {
		FontUIResource globalFont = new FontUIResource(defaultFont);
		for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, globalFont);
			}
		}
	}

	/**
	 * ����Ϣ�Ի���
	 * 
	 * @param msg
	 *            ��Ϣ����
	 */
	public static void messageBox(String msg) {
		JOptionPane.showMessageDialog(null, msg, "��ʾ", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * ������Ի���
	 * 
	 * @param msg
	 *            ��Ϣ����
	 * @param hint
	 *            �����ʼ����
	 * @param title
	 *            �Ի������
	 * @return ����û����ȷ���������ַ���������Ϊ""�������򷵻�null
	 */
	public static String inputBox(String msg, String hint, String title) {
		JTextArea text = new JTextArea(hint, 5, 50);
		text.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(text);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Object[] message = { msg, scrollPane };
		JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		Object option = pane.getValue();
		if (option != null && (int) option == JOptionPane.OK_OPTION)
			return text.getText();
		else
			return null;
	}

}

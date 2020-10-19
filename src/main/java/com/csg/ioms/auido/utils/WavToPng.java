package com.csg.ioms.auido.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavToPng {

	/**
	 * wav音频文件转png图片
	 * 
	 * @param wavfilePath
	 * @param pngFilePath
	 */
	public static synchronized void getPng(String wavfilePath, String pngFilePath) {
		try {
			int width = 4000;
			int height = 500;
			List<Short> deque = new ArrayList<Short>();
			AudioInputStream ais;
			File wavFile=new File(wavfilePath);
			ais = AudioSystem.getAudioInputStream(wavFile);
			printFormat(ais.getFormat());
			byte[] buf = new byte[4];
			while ((ais.read(buf)) != -1) {
				//双声道
				if (ais.getFormat().getChannels() == 2) {
					if (ais.getFormat().getSampleSizeInBits() == 16) {
						deque.add((short) ((buf[1]) | buf[0] >> 8));// 左声道
					} else {
						deque.add((short) buf[3]);// 左声道
					}
				} else {//单声道
					if (ais.getFormat().getSampleSizeInBits() == 16) {
						deque.add((short) (buf[1] | buf[0] >> 8));
						//deque.add((short) (buf[3]|buf[2]>>8));
					} else {
						deque.add((short) buf[0]);
						deque.add((short) buf[1]);
						deque.add((short) buf[2]);
						deque.add((short) buf[3]);
					}
				}
			}
			// 高度倍数
			int heightRate = 2;
			// 宽度步长
			int widthRate = 8;
			width = deque.size() / widthRate;
			BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = buffered.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.red);
			g.translate(10, height / 2);
			if (deque.size() > 1) {
				int x1 = 0, x2 = 0;
				for (int i = 0; i < deque.size() - 1; i = i + widthRate) {
					g.drawLine(x1, (int) (deque.get(i) * heightRate), x2, (int) (deque.get(i + 1) * heightRate));
					x1 = x2;
					x2 = x2 + 1;
				}
			}
			g.dispose();
			// 输出png图片
			ImageIO.write(buffered, "png", new File(pngFilePath));
			ais.close();
			buffered.flush();
		} catch (Exception e) {
			throw new RuntimeException("音频转换图像失败");
		}
	}

	public static void printFormat(AudioFormat format) {
		System.out.println(format.getEncoding() + " => " + format.getSampleRate() + " hz, "
				+ format.getSampleSizeInBits() + " bit, " + format.getChannels() + " channel, " + format.getFrameRate()
				+ " frames/second, " + format.getFrameSize() + " bytes/frame");
	}

}

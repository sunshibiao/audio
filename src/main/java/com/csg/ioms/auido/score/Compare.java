package com.csg.ioms.auido.score;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.csg.ioms.auido.data.AudioDataOperate;

/**
 * 音频相似度对比
 * 
 * @author Administrator
 *
 */
public class Compare {

	private String standardAudioFilePath;
	private String compareAudioFilePath;

	String recordPath = "";

	// 保存标准音频的数据
	private double[] standardAudioData;
	// 能量图
	private double[] standardEnergyData;
	// 有效数据
	private double[] standardUsefulData;
	// 保存对比音频的数据
	private double[] compareAudioData;
	private double[] compareEnergyData;
	private double[] compareUsefulData;

	public static void main(String[] args) throws IOException {
		Compare compare = new Compare();
		File directory = new File("src/main/resources");
		String courseFile = directory.getCanonicalPath();
		System.out.println(courseFile);
		compare.standardAudioFilePath = courseFile + "\\file\\2.m4a";
		compare.compareAudioFilePath = courseFile + "\\file\\3.m4a";
		compare.showAudioWave();
		compare.showFilterWave();
		compare.showEnergyWave();
		compare.showUsefulWave();
		String result = compare.calculateCompareResult();
		System.out.println(result);
		compare.drawWave(compare.standardUsefulData, courseFile + "\\file\\1.png");
		compare.drawWave(compare.compareUsefulData, courseFile + "\\file\\1_1.png");
	}

	/**
	 * 获取音频数据
	 *
	 * @param filePath 音频数据文件路径
	 * @return
	 */
	public short[] getAudioData(String filePath) {
		File file = new File(filePath);
		System.out.println("File info   " + file.length());
		DataInputStream dis = null;
		short[] audioData = null;
		try {
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			// file.length() / 2 +1 : /2 : 两位byte数据保存为一位short数据; +1 : 保存文件结尾标志
			audioData = AudioDataOperate.getAudioData(dis, (int) file.length() / 2, AudioDataOperate.TYPE_16BIT);
			dis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return audioData;
	}

	/**
	 * 显示原始波形（经过归一化处理）
	 *
	 * @param type
	 */
	private void showAudioWave() {
		short[] data = getAudioData(standardAudioFilePath);
		standardAudioData = AudioDataOperate.normalize(data);
		short[] data1 = getAudioData(compareAudioFilePath);
		compareAudioData = AudioDataOperate.normalize(data1);
	}

	/**
	 * 显示滤波后波形
	 *
	 * @param type
	 */
	private void showFilterWave() {
		// y(n) = 1*x(n)+(-0.9375)*x(n-1) 滤波
		standardAudioData = AudioDataOperate.filter(standardAudioData, 1, -0.9375);
		standardAudioData = AudioDataOperate.normalize(standardAudioData);
		// y(n) = 1*x(n)+(-0.9375)*x(n-1) 滤波
		compareAudioData = AudioDataOperate.filter(compareAudioData, 1, -0.9375);
		compareAudioData = AudioDataOperate.normalize(compareAudioData);
	}

	/**
	 * 显示短时能量波形
	 *
	 * @param type
	 */
	private void showEnergyWave() {
		double[] dotProductData = AudioDataOperate.dotProduct(standardAudioData);
		double[] wins = AudioDataOperate.generateHammingWindows(32, 16);
		double[] convValue = AudioDataOperate.conv(dotProductData, wins);
		standardEnergyData = AudioDataOperate.normalize(convValue);
		double[] dotProductData1 = AudioDataOperate.dotProduct(compareAudioData);
		double[] wins1 = AudioDataOperate.generateHammingWindows(32, 16);
		double[] convValue1 = AudioDataOperate.conv(dotProductData1, wins1);
		compareEnergyData = AudioDataOperate.normalize(convValue1);
	}

	/**
	 * 显示截取有效短时能量数据波形
	 *
	 * @param type
	 */
	private void showUsefulWave() {
		standardUsefulData = AudioDataOperate.getUsefulData(standardEnergyData);
		compareUsefulData = AudioDataOperate.dealCompareData(compareEnergyData, standardUsefulData.length);
	}

	/**
	 * 计算对比结果
	 */
	private String calculateCompareResult() {
		if (null == standardUsefulData || null == compareUsefulData) {
			return "0";
		}
		final double result = AudioDataOperate.cosineDistance(standardUsefulData, compareUsefulData);
		System.out.println(result);
		return (result * 100) + "%";
	}

	/**
	 * 画图
	 * 
	 * @param canvas
	 * @throws IOException
	 */
	private void drawWave(double[] audioData, String pngPath) {
		try {
			int mViewHeight = 800;
			int width = 1200;
			int height = 500;
			BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = buffered.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.red);
			g.translate(10, height / 2);
			float stepSize = (float) ((double) 1000 / audioData.length);
			for (int i = 10; i < audioData.length; i++) {
				if (i % 10 == 0) {
					g.drawLine((int) ((i - 10) * stepSize),
							(int) (mViewHeight / 2 - (float) (audioData[i - 10] * mViewHeight / 2)),
							(int) (i * stepSize), (int) (mViewHeight / 2 - (float) (audioData[i] * mViewHeight / 2)));
				}
			}
			g.dispose();
			// 输出png图片
			ImageIO.write(buffered, "png", new File(pngPath));
			buffered.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

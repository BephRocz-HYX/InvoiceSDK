package hyx.invoice.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.suggest.Suggester;

import hyx.invoice.util.Similarity;
import hyx.invoice.util.SortMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 发票明细自然语言处理
 * 
 * @author HYX
 *
 */
public class InvoiceNLP {

	private static JSONObject inWords = new JSONObject();
	private static JSONObject outWords = new JSONObject();
	private static Map<String, Integer> companyClass = new HashMap<String, Integer>();
	private static Suggester suggesterAll = new Suggester();
	private static Suggester suggesterTemp = new Suggester();
	private static String dataPath;

	protected static void setDataPath(String dataPath) {
		InvoiceNLP.dataPath = dataPath;
	}

	protected static void setCompanyClass(Map<String, Integer> companyClass) {
		InvoiceNLP.companyClass = companyClass;
	}

	protected static void init() {

		try {
			setInORoutWords("in");
			setInORoutWords("out");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Iterator iterator = outWords.keys();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			suggesterAll.addSentence(outWords.getString(key));
		}

	}

	private static String getKey(Map m, String value) {
		Iterator in = m.keySet().iterator();
		while (in.hasNext()) {
			String key = in.next().toString();
			if (value.equals(m.get(key)))
				return key;
		}

		return "";
	}

	private static void removeDuplicate(List<String> list) {
		LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
		set.addAll(list);
		list.clear();
		list.addAll(set);
	}

	private static String keyWord(String text) {
		List<String> keys = HanLP.extractKeyword(text, 5);
		String result = "";
		for (String k : keys)
			result += (k + "，");
		return result.substring(0, result.length() - 1);
	}

	private static String getWord(String word) {
		if (!word.contains("详见销货清单")) {
			if (word.contains("冷轧板卷")) {
				return "冷轧板卷";
			} else {
				if (word.contains("激光拼焊板")) {
					return "激光拼焊板";
				} else {
					if (word.contains("[京东超市]")) {
						try {
							String word1 = word.substring(word.indexOf("[京东超市]") + 6, word.indexOf(" "));
							return word1;
						} catch (Exception e) {

						}
						return keyWord(word);
					} else {
						String reg = "[\\u4e00-\\u9fa5]+";
						Pattern p = Pattern.compile("\\*(.*?)\\*");
						Matcher m = p.matcher(word);
						if (m.find()) {
							String word1 = word.substring(word.indexOf("*", 1) + 1);
							// if (word1.matches(reg))
							if (word1.contains("/")) {
								String word2 = word1.substring(0, word1.indexOf("/"));
								return word2;
							} else {
								if (word1.contains("(")) {
									String word3 = word1.substring(0, word1.indexOf("("));
									return word3;
								} else {
									if (word1.contains("（")) {
										String word4 = word1.substring(0, word1.indexOf("（"));
										return word4;
									} else {
										return word1;
									}
								}
							}

						} else {
							if (word.length() <= 21) {
								return word;
							} else {
								if (word.contains("-")) {
									return word;
								} else {
									if (word.contains(" ")) {
										return word.split(" ")[0];
									} else {
										try {
											return keyWord(word);
										} catch (Exception e) {
											return "";
										}
									}
								}
							}
						}
					}
				}
			}
		} else {
			return "";
		}
	}

	protected static JSONArray getInORoutWords(String inORout) throws FileNotFoundException {
//		Map<String, String> result = new HashMap<String, String>();
		JSONArray result = new JSONArray();

		// InputStream is = null;
		File file = null;
		try {
			if (inORout.equals("in"))
				file = new File(dataPath + "in.txt");
			if (inORout.equals("out"))
				file = new File(dataPath + "out.txt");
		} catch (Exception e) {
			System.out.println("读文件出错：" + e.getMessage());
		}
		// InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		try {

			while (null != (line = br.readLine())) {
				JSONObject temp = new JSONObject();
				temp.put("id", line.split(",")[1]);
				temp.put("data", line.split(",")[0]);
				result.add(temp);
//				result.put(line.split(",")[0], line.split(",")[1]);
			}
			br.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("每行每行读in出错：" + e.getMessage());
			return null;
		}

	}

	private static JSONObject getInORoutWords(String inORout, JSONArray words) {
		JSONObject result = new JSONObject();
		try {
			Iterator iterator = companyClass.keySet().iterator();

			while (iterator.hasNext()) {
				String id = iterator.next().toString();
				String data = "";
				for (Object temp : words) {
					try {
						JSONObject t = JSONObject.fromObject(temp);
						String word = t.getString("data");
						String key = t.getString("id");
						if (id.equals(key)) {
							String li = word.replace("\t", " ").replace("\n", " ");
							data += li + "，";
						}
					} catch (Exception e) {

					}

				}
				if (!data.isEmpty())
					result.put(id, data);
				data = "";
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private static void setInORoutWords(String inORout) throws FileNotFoundException {
		JSONArray result = new JSONArray();
		File file = null;
		try {
			if (inORout.equals("in"))
				file = new File(dataPath + "InWordResultFinal.txt");
			if (inORout.equals("out"))
				file = new File(dataPath + "OutWordResultFinal.txt");
		} catch (Exception e) {
			System.out.println("读文件出错：" + e.getMessage());
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		try {
			while (null != (line = br.readLine())) {
				if (!line.isEmpty()) {
					String key = line.split("\t")[0];
					String temp = line.split("\t")[1];
					if (inORout.equals("in"))
						inWords.put(key, temp);
					if (inORout.equals("out"))
						outWords.put(key, temp);
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println("每行每行读文件出错：" + e.getMessage());

		}

	}

	protected static Map<String, Integer> setClass() throws FileNotFoundException {
		Map<String, Integer> companyClass = new HashMap<String, Integer>();
		File file = null;
		try {
			file = new File(dataPath + "企业行业代码.txt");
		} catch (Exception e) {
			System.out.println("读文件出错：" + e.getMessage());
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		try {
			while (null != (line = br.readLine())) {
				String key = line.split(",")[0];
				int temp = Integer.parseInt(line.split(",")[1]);
				companyClass.put(key, temp);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("每行每行读文件出错：" + e.getMessage());

		}
		return companyClass;
	}

	private static String getClass(String word) {
		Pattern p = Pattern.compile("\\*(.*?)\\*");
		Matcher m = p.matcher(word);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

	private static String cleanWord(String word) {
		Pattern p = Pattern.compile("([\\u4e00-\\u9fa5]+)[a-zA-Z0-9\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥× ]+");
		Pattern p1 = Pattern.compile("[a-zA-Z0-9\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥× ]+([\\u4e00-\\u9fa5]+)");
		Matcher m = p.matcher(word);
		if (m.find()) {
			if (m.group(1).length() >= 2)
				return m.group(1);
			else {
				if (m.group(1).equals("床"))
					return m.group(1);
			}
		}
		Matcher m1 = p1.matcher(word);
		if (m1.find()) {
			if (m1.group(1).length() >= 2)
				return m1.group(1);
			else {
				if (m1.group(1).equals("床"))
					return m1.group(1);
			}
		}
		return word;
	}

	// 文本预处理
	private static String preWord(String[] words) {
		String temp = "";
		for (String wo : words) {
			if (!getClass(wo).isEmpty())
				temp = temp + "；" + getClass(wo) + "，" + cleanWord(getWord(wo)).replace(" ", "");
			else {
				if (!cleanWord(getWord(wo)).isEmpty())
					temp = temp + "；" + cleanWord(getWord(wo)).replace(" ", "");
				else {
					if (!wo.contains("详见销货清单"))
						temp = temp + "；" + wo.trim();
				}
			}
		}
		return temp.substring(1);
	}

	// 文本预处理
	private static String preWord(String wo) {
		String temp = "";

		if (!getClass(wo).isEmpty())
			temp = temp + "；" + getClass(wo) + "，" + cleanWord(getWord(wo)).replace(" ", "");
		else {
			if (!cleanWord(getWord(wo)).isEmpty())
				temp = temp + "；" + cleanWord(getWord(wo)).replace(" ", "");
			else {
				if (!wo.contains("详见销货清单"))
					temp = temp + "；" + wo.trim();
			}
		}

		return temp.substring(1);
	}

	protected static void setInORoutWord(String inORout, JSONArray Words) throws IOException {
		JSONObject test = getInORoutWords(inORout, Words);
		FileWriter writer;
		if (inORout.equals("in"))
			writer = new FileWriter(dataPath + "InWordResultFinal.txt", true);
		else
			writer = new FileWriter(dataPath + "OutWordResultFinal.txt", true);

		Iterator it=test.keys();
		while (it.hasNext()) {
			String num = it.next().toString();
			String words = test.getString(num);
			String[] word = words.split("，");
			String temp = "";
			for (String wo : word) {
				if (!getClass(wo).isEmpty())
					temp = temp + "；" + getClass(wo) + "，" + cleanWord(getWord(wo)).replace(" ", "");
				else {
					if (!cleanWord(getWord(wo)).isEmpty())
						temp = temp + "；" + cleanWord(getWord(wo)).replace(" ", "");
					else {
						if (!wo.contains("详见销货清单"))
							temp = temp + "；" + wo.trim();
					}
				}
			}
			try {
				String wo = temp.substring(1) + ".";
				String[] in = wo.split("，|；");
				List<String> inList = new ArrayList<String>();
				for (String iString : in)
					inList.add(iString);
				removeDuplicate(inList);
				wo = "";
				for (String k : inList)
					wo += k + "，";
				writer.write(num + "\t" + wo.substring(0, wo.length() - 1) + "\n");
			} catch (Exception e) {

			}
		}
		writer.close();
	}

	private static List<String> getOutWordSimilarity(String outWord, Suggester suggester) {

		return suggester.suggest(outWord, 5); // 语义

	}

	private static List<String> matchOutWord(String outWord) {

		Iterator out = outWords.keySet().iterator();
		String[] outw = outWord.split("，|；");// 传入的销项
		List<String> outList = Arrays.asList(outw);
		Map aMap = new HashMap<>();
		while (out.hasNext()) {
			String key = out.next().toString();
			String in = outWords.getString(key);
			String[] inw = in.split("，|；");
			List<String> inList = Arrays.asList(inw);
			double totalScore = 0.0;
			int totalNum = outList.size();
			for (String o : outList) {
				List<Double> score = new ArrayList<Double>();
				for (String i : inList) {
					i = i.replace(".", "");
					Similarity.setSimilarity(o, i);
					double greater = Math.max(CoreSynonymDictionary.similarity(o, i), Similarity.sim());
					score.add(greater);
				}
				double max = Collections.max(score);
				totalScore += max;
			}
			double result = 0;
			try {
				result = totalScore / totalNum * 100.0;
			} catch (Exception e) {

			}
			aMap.put(result, key);
		}

		Map k = SortMap.sortMapByKey(aMap);
		Iterator it = k.keySet().iterator();
		int count = 0;
		List<String> result = new ArrayList<String>();
		while (it.hasNext()) {
			double key = Double.parseDouble(it.next().toString());
			String outword = k.get(key).toString();
			result.add(outWords.getString(outword));
			count++;
			if (count > 2)
				break;
		}

		return result;
	}

	protected static List<String> initOutWords(int compClass, String[] outWord) {
		Iterator it = companyClass.keySet().iterator();
		Iterator out = outWords.keySet().iterator();
		List<String> outword = new ArrayList<String>();
		while (it.hasNext()) {
			String key = it.next().toString();
			int num = companyClass.get(key);
			if (compClass == num) {
				while (out.hasNext()) {
					String outkey = out.next().toString();
					if (outkey.equals(key))
						suggesterTemp.addSentence(outWords.getString(key));
					// outword.add(outWords.get(key));
				}
			}
		}
		outword = getOutWordSimilarity(preWord(outWord), suggesterTemp);
		if (outword.isEmpty())
			outword = getOutWordSimilarity(preWord(outWord), suggesterAll);

		return outword;
	}

	protected static double matchInWord(String inWord, List<String> outword) {
		String temp = "";
		for (String outw : outword) {
			String key = getKey(outWords, outw);
			temp += inWords.get(key) + "，";// 获取库中对应inWords里的值拼起来
		}
		String[] in = temp.split("，|；");
		List<String> inList = new ArrayList<String>();
		for (String iString : in)
			inList.add(iString);
		removeDuplicate(inList);
		String inTemp = preWord(inWord);// 预处理
		String[] out = inTemp.split("，|；");// 传入的进项
		List<String> outList = Arrays.asList(out);
		double totalScore = 0.0;
		int totalNum = outList.size();
		for (String o : outList) {
			List<Double> score = new ArrayList<Double>();
			for (String i : inList) {
				i = i.replace(".", "");
				Similarity.setSimilarity(o, i);
				double greater = Math.max(CoreSynonymDictionary.similarity(o, i), Similarity.sim());
				score.add(greater);
			}
			double max = Collections.max(score);
			totalScore += max;
		}
		double result = 0;
		try {
			result = totalScore / totalNum * 100.0;
		} catch (Exception e) {

		}
		return result;
	}

}

package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSON;


public class GGZDUpdate {

	@SuppressWarnings({ "unchecked", "rawtypes"})
	public static void main(String[] args) {
		Map data = initData();
		Map<String, Object> finalResultMap = new HashMap<>();

		List<Map<String, Object>> ltgbList = (List<Map<String, Object>>) data.get("ltgb");
		if (ltgbList != null && ltgbList.size() > 0) {
			Integer diffDateFlag = (Integer) ltgbList.get(0).get("DiffDateFlag");
			if (diffDateFlag == 1) {

			} else {
				String year ="",ggzdNameCode="";
				List<Map<String, Object>> zhpsfsList = (List<Map<String, Object>>) data.get("zhpsfs");
				if (zhpsfsList != null && zhpsfsList.size() > 0) {
					year=zhpsfsList.get(0).get("EndDate") == null ? "--" : zhpsfsList.get(0).get("EndDate").toString();
					ggzdNameCode=data.get("addAbbr") + " " + data.get("addCode");
				}
				
				String score ="", sc_rank="",TotalNum="";
				List<Map<String, Object>> zhpsQscPmList = (List<Map<String, Object>>) data.get("zhpsQscPm");
				if (null != zhpsQscPmList && zhpsQscPmList.size() > 0) {
					Integer marketRank = zhpsQscPmList.get(0).get("MarketRank") == null ? 0: (Integer) zhpsQscPmList.get(0).get("MarketRank");
					Integer totalNum = zhpsQscPmList.get(0).get("TotalNum") == null ? 0: (Integer) zhpsQscPmList.get(0).get("TotalNum");
					Double finalValue = zhpsQscPmList.get(0).get("FinalValue") == null ? 0: ((BigDecimal) zhpsQscPmList.get(0).get("FinalValue")).doubleValue();
				
					score = finalValue != null ? toFixed(finalValue, 0): toFixed(new Double(marketRank / totalNum * 100), 2);
					sc_rank = totalNum - marketRank + 1+"";
					TotalNum= totalNum.toString();
				}
				
				String hy_rank ="",hy_rank1="";
				List<Map<String, Object>> zhssRankList = (List<Map<String, Object>>) data.get("zhssRank");
				if (zhssRankList != null && zhssRankList.size() > 0) {
					String rankStr = zhssRankList.get(0).get("Rank") == null ? "0/0"
							: (String) zhssRankList.get(0).get("Rank");
					Integer hyRankfz = Integer.valueOf(rankStr.split("/")[0]);
					Integer hyRankfm = Integer.valueOf(rankStr.split("/")[1]);
					hy_rank =hyRankfz.toString();
					hy_rank1=hyRankfm.toString();
				}
				
				finalResultMap.put("hy_rank", hy_rank);
				finalResultMap.put("hy_rank1", hy_rank1);
				finalResultMap.put("score", score);
				finalResultMap.put("TotalNum", TotalNum);
				finalResultMap.put("sc_rank", sc_rank);
				finalResultMap.put("year", year);
				finalResultMap.put("ggzdNameCode", ggzdNameCode);
				



				// ============================================================================短期评语


				/* 财务健康评语 */
				String cwjkfs = "-";
				List<Map<String, Object>> cwjkQscPmList = (List<Map<String, Object>>) data.get("cwjkQscPm");
				List<Map<String, Object>> cwjkfsList = (List<Map<String, Object>>) data.get("cwjkQscPm");
				if (null != cwjkfsList && cwjkfsList.get(0) != null) {
					if (cwjkQscPmList.get(0).get("Value") != null&& !cwjkQscPmList.get(0).get("Value").toString().equals("")) {
						Double marketRank = Double.valueOf(getIntegerField(cwjkQscPmList.get(0), "MarketRank"));
						Double totalNum = Double.valueOf(getIntegerField(cwjkQscPmList.get(0), "TotalNum"));

						Double cwjkhf = marketRank / totalNum;
						Double cwjkVal = cwjkhf * 100;

						if (cwjkQscPmList.get(0).get("Value") != null) {
							cwjkfs = toFixed(getDoubleField(cwjkQscPmList.get(0), "Value"), 0);
						} else {
							cwjkfs = toFixed(cwjkVal, 0);
						}
					}
				}

				// cwjkRank

				String cwjkRank = "-";
				List<Map<String, Object>> cwjkRankList = (List<Map<String, Object>>) data.get("cwjkRank");
				if (cwjkRankList != null && cwjkRankList.get(0) != null) {
					Object ed = cwjkRankList.get(0).get("EndDate");
					if (ed != null) {
						if (StringUtils.isNotBlank(ed.toString())) {
							cwjkRank = cwjkRankList.get(0).get("Rank").toString();
						}
					}
				}
				
				finalResultMap.put("cwjkRank", cwjkRank);
				finalResultMap.put("cwjkfs", cwjkfs);


				/* 价值评估评语 */
				String jzpgfs = "-";
				List<Map<String, Object>> scrdQscPmList = (List<Map<String, Object>>) data.get("scrdQscPm");
				if (null != scrdQscPmList && scrdQscPmList.size() > 0) {
					Integer jzpgRank = scrdQscPmList.get(0).get("JzpgRank") == null ? 0
							: (Integer) scrdQscPmList.get(0).get("JzpgRank");
					Integer totalNum = scrdQscPmList.get(0).get("TotalNum") == null ? 0
							: (Integer) scrdQscPmList.get(0).get("TotalNum");
					double jzpg = jzpgRank / totalNum * 100;
					Object jzpgValue = scrdQscPmList.get(0).get("JzpgValue");
					if (jzpgValue != null) {
						jzpgfs = toFixed(((BigDecimal) jzpgValue).doubleValue(), 0);
					} else {
						jzpgfs = toFixed(jzpg, 0);
					}

				}
				

				String jzpgRank = "-";
				List<Map<String, Object>> jzpgRankList = (List<Map<String, Object>>) data.get("jzpgRank");
				if (null != jzpgRankList && null != jzpgRankList.get(0)) {
					Object ed = jzpgRankList.get(0).get("EndDate");
					if (null != ed && StringUtils.isNotBlank(ed.toString())) {
						jzpgRank = jzpgRankList.get(0).get("Rank") == null ? "-": (String) jzpgRankList.get(0).get("Rank");
					}
				}
				finalResultMap.put("jzpgfs", jzpgfs);
				finalResultMap.put("jzpgRank", jzpgRank);
				
				//==============================================================行情表现评
				String hqbxfs ="-";
				if (null != scrdQscPmList && scrdQscPmList.size() > 0) {
					Object hq = scrdQscPmList.get(0).get("HqbxRank");
					if(null != hq && !hq.toString().equals("")) {
						Integer hqbxRank = scrdQscPmList.get(0).get("HqbxRank")==null?0:(Integer) scrdQscPmList.get(0).get("HqbxRank");
						Integer totalNum = scrdQscPmList.get(0).get("TotalNum")==null?0:(Integer) scrdQscPmList.get(0).get("TotalNum");
						Double hqbxValue = scrdQscPmList.get(0).get("HqbxValue")==null?null:((BigDecimal) scrdQscPmList.get(0).get("HqbxValue")).doubleValue();
						Double value = hqbxRank * 1d /totalNum * 100;
						if(hqbxValue !=null) {
							hqbxfs = toFixed(hqbxValue, 0);
						}else {
							hqbxfs = toFixed(value, 0);
						}
					}
				}
				
				//hqbxRank
				String hqbxRank = "-";
				List<Map<String, Object>> hqbxRankList = (List<Map<String, Object>>) data.get("hqbxRank");
				if(null != hqbxRankList && hqbxRankList.get(0) != null) {
					Object ed = hqbxRankList.get(0).get("EndDate");
					if(null != ed && StringUtils.isNotBlank(ed.toString())) {
						hqbxRank = hqbxRankList.get(0).get("Rank") == null?"-":hqbxRankList.get(0).get("Rank").toString();
					}
				}
				
				finalResultMap.put("hqbxRank", hqbxRank);
				finalResultMap.put("hqbxfs", hqbxfs);
				
				
				//==============================================市场热度评语=======================

                /*市场热度分数及排名*/
				String scrdfs="-";
				String scrdRank = "-";
				List<Map<String, Object>> scrdfsList = (List<Map<String, Object>>) data.get("scrdfs");
				if(null != scrdfsList && scrdfsList.get(0) != null) {
					if(scrdQscPmList.get(0).get("MarketValue")!= null && !scrdQscPmList.get(0).get("MarketValue").toString().equals("")) {
						Double marketValue = getDoubleField(scrdQscPmList.get(0), "MarketValue");
						scrdfs=toFixed(marketValue,0);
					}
				}
				List<Map<String, Object>> scrdRankList = (List<Map<String, Object>>) data.get("scrdRank");
				if(null != scrdRankList && scrdRankList.get(0)!=null) {
					Object ed = scrdRankList.get(0).get("EndDate");
					if(ed != null && !ed.toString().equals("null")&& !ed.toString().equals("")) {
						scrdRank = scrdRankList.get(0).get("Rank")==null?"-":scrdRankList.get(0).get("Rank").toString();
					}
				}
				finalResultMap.put("scrdRank", scrdRank);
				finalResultMap.put("scrdfs", scrdfs);
			}
		}
		
		
//		短期评语： 近一月，市场关注度较强，相关新闻85条，2018-01-19日创最近一月新高，走势强于大盘，弱于行业；短线观望 OK
//		中长期评语： 该股为大盘中价股，综合财务状况较差，毛利率与行业平均水平持平，净资产收益率低于行业平均水平，在银行行业中估值合理，可适当关注 ok
//		财务健康评语：综合财务状况较差，业绩小幅增长，现金流不足 --ok
//		价值评估评语：在银行中综合价值相对被高估，股价对其增长预期已有一定的反应，后市超预期增长的可持续性值得考量，其中市销率、市净率、市现率、市盈率等在行业中排名靠前-ok
//		行情表现评语:近一个月走势强于大盘，活跃度一般，近一周大单净流入占周成交额-0.33% ok
//		市场热度评语：近3个月，6篇调高评级，最新评级买入，目标价大于最新价，上涨空间35%，媒体关注度较高ok
		
		
//		ggzdNameCode 	: 平安银行 000001 ok
//		year 	: 2018-01-25 ok
//		cwjkRank 	: 15/26  ok
//		hy_rank 	: 22 ok
//		scrdfs 	: 90 ok
//		hy_rank1 	: 26 ok
//		cwjkfs 	: 39 ------no  已修改 ok
//		scrdRank 	: 11/26  ---ok
//		score 	: 78 ok
//		hqbxfs 	: 63 ok
//		TotalNum 	: 3502 no
//		sc_rank 	: 792  ???
//		jzpgfs 	: 91  ok
//		hqbxRank 	: 23/26  ok

		for (String key : finalResultMap.keySet()) {
			System.err.println(key + " \t: "+ finalResultMap.get(key));
		}

	}

	private static Double[] convertDoubleArr(String[] arr) {
		Double[] ds = new Double[arr.length];
		for(int i=0; i<arr.length;i++) {
			ds[i] = Double.valueOf(arr[i]);
		}
		return ds;
	}

	private static Integer getIntegerField(Map<String, Object> map, String field) {
		return map.get(field) == null ? 0 : ((Integer) map.get(field));
	}

	private static double getDoubleField(Map<String, Object> map, String field) {
		return map.get(field) == null ? 0d : ((BigDecimal) map.get(field)).doubleValue();
	}

	public static String toFixed(Double v, int w) {
		if (null != v) {
			String suffix = w == 0 ? "" : ".";
			for (int i = 0; i < w; i++) {
				suffix += "0";
			}
			DecimalFormat df = new DecimalFormat("#,##0" + suffix);
			return df.format(v);
		}
		return null;
	}

	private static final ThreadLocal<SimpleDateFormat> localDateFormat = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}

	};

	public static int dayDiff(String s, String e) {
		try {
			long longs = localDateFormat.get().parse(s).getTime();
			long longe = localDateFormat.get().parse(e).getTime();
			int diff = (int) (Math.abs(longs - longe) / (1000 * 60 * 60 * 24));
			return diff;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return 0;
	}

	@SuppressWarnings("rawtypes")
	private static Map initData() {
		File file = null;
		try {
			file = ResourceUtils.getFile("classpath:000001_ggzd.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map map = null;
		try {
			FileInputStream in = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(in);
			BufferedReader bufferedReader = new BufferedReader(reader);
			StringBuffer sb = new StringBuffer();
			String content = null;
			while ((content = bufferedReader.readLine()) != null) {
				sb.append(content);
			}

			map = JSON.parseObject(sb.toString());

			reader.close();
			bufferedReader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

}

package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSON;


public class GGZD {

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
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
				
				
				//加入热门概念
				String hotConcept = "";
				List<Map<String, Object>> hotConceptList = (List<Map<String, Object>>) data.get("hotConcept");
				if(hotConceptList != null && hotConceptList.get(0) != null) {
					hotConcept= hotConceptList.get(0).get("ConceptName").toString();
				}
				
				List<Map<String, Object>> fiveDayFundList = (List<Map<String, Object>>) data.get("fiveDayFund");
				Double fiveFundValue =0d;
				String fundStr ="";
				if(null != fiveDayFundList && fiveDayFundList.get(0).get("fundflow") != null) {
					List<Map<String, Object>> fiveDayList = (List<Map<String, Object>>) fiveDayFundList.get(0).get("fundflow");
					for(Map<String, Object> map : fiveDayList) {
						Map<String,Object> superGrp = (Map<String, Object>) map.get("super_grp");
						Map<String,Object> largeGrp = (Map<String, Object>) map.get("large_grp");
						Integer sin = superGrp.get("turnover_in")==null?0:(Integer) superGrp.get("turnover_in");
						Integer lin = largeGrp.get("turnover_in")==null?0:(Integer) largeGrp.get("turnover_in");
						Integer sout = superGrp.get("turnover_out")==null?0:(Integer) superGrp.get("turnover_out");
						Integer lout = largeGrp.get("turnover_out")==null?0:(Integer) largeGrp.get("turnover_out");
						
						
						//System.out.println(sin +" "+ lin + " "+ sout + " "+ lout + " "+ map.get("date"));
						Double value = (sin + lin - sout -lout) * 0.1/ 10000000;
						fiveFundValue+= value;
					}
					fundStr = "近5日主力资金净流入"+toFixed(fiveFundValue, 2)+"亿，";
				}
				System.out.println(fundStr);
				
				//5日主力的资金流动
				

				// hqbxpy
				String mce ="",mhyce="",cxg="",scgzd="",jgdy="",xwts="",xwtspy="",ljrdTotal="",zjlrzbpy="",lhbcspy="",lhbjlrpy="";
						
				List<Map<String, Object>> hqbxpyList = (List<Map<String, Object>>) data.get("hqbxpy");
				if (null != hqbxpyList && hqbxpyList.size() > 0) {
					Map<String, String[]> diffMap = new HashMap<String, String[]>();
					diffMap.put("mce", new String[] { "走势强于大盘，", "走势弱于大盘，" });
					diffMap.put("mhyce", new String[] { "强于行业；", "弱于行业；" });
					diffMap.put("cxg", new String[] { "股价创%s日新高，", "股价创%s日新高，" });
					diffMap.put("lhbcs", new String[] { "。机构席位龙虎榜上榜%s次", "。机构席位龙虎榜上榜%s次" });
					diffMap.put("lhbjlr", new String[] { "，净流入%s亿", "，净流出%s亿" });
					diffMap.put("zjlrzb", new String[] { "，近一周大单净流入占周成交额%s%%", "，近一周大单净流入占周成交额%s%%" });

					for (Map<String, Object> map : hqbxpyList) {
						String factorType = (String) map.get("FactorType");
						Double value = map.get("Value") == null ? null : ((BigDecimal) map.get("Value")).doubleValue();
						String[] diffArr = diffMap.get(factorType);
						if (null == diffArr) {
							continue;
						}
						String str = value >= 0 ? diffArr[0] : diffArr[1];
						if (factorType.equals("mce")) {
							mce = str;
						}else if (factorType.equals("mhyce")) {
							mhyce = str;
						}else if (factorType.equals("cxg")) {
							cxg =String.format(str, new Object[] { toFixed(value, 0) });
						} else if (factorType.equals("lhbcs")) {
							lhbcspy =String.format(str, new Object[] { toFixed(value, 0) });
						} else if (factorType.equals("lhbjlr")) {
							lhbjlrpy =String.format(str, new Object[] { toFixed(Math.abs(value), 2) });
						} else if (factorType.equals("zjlrzb")) {
							zjlrzbpy=String.format(str, new Object[] { toFixed(value, 2) });
						}
					}

				}

				String scgzd2 ="";
				List<Map<String, Object>> scrdQscPmList = (List<Map<String, Object>>) data.get("scrdQscPm");
				if (null != scrdQscPmList && scrdQscPmList.size() > 0) {
					Integer marketRank = scrdQscPmList.get(0).get("MarketRank") == null ? 0
							: (Integer) scrdQscPmList.get(0).get("MarketRank");
					Integer totalNum = scrdQscPmList.get(0).get("TotalNum") == null ? 0
							: (Integer) scrdQscPmList.get(0).get("TotalNum");
					double rdfd = marketRank / totalNum;
					
					if (0 < rdfd && rdfd <= 0.4) {
						scgzd = "，市场关注度不足，";
						scgzd2 = "市场关注度不足；";
					} else if (0.4 < rdfd && rdfd <= 0.7) {
						scgzd = "，市场关注度一般，";
						scgzd2 = "市场关注度一般；";
					} else {
						scgzd = "，市场关注度较强，";
						scgzd2 = "市场关注度较高；";
					}
				}

				List<Map<String, Object>> scrdpyList = (List<Map<String, Object>>) data.get("scrdpy");
				if (null != scrdpyList && scrdpyList.size() > 0) {
					Map<String, String[]> diffMap = new HashMap<String, String[]>();
					diffMap.put("jgdy", new String[] { "有%s家机构对该股进行了调研，" });
					diffMap.put("xwts", new String[] { "相关新闻%s条，" });

					for (Map<String, Object> map : scrdpyList) {
						String factorType = (String) map.get("FactorType");
						Double value = map.get("Value") == null ? null : ((BigDecimal) map.get("Value")).doubleValue();
						String[] diffArr = diffMap.get(factorType);
						if (null == diffArr) {
							continue;
						}
						String str = value >= 0 ? diffArr[0] : diffArr[1];
						if (factorType.equals("jgdy")) {
							jgdy=String.format(str, new Object[] { toFixed(value, 0) });
						} else if (factorType.equals("xwts")) {
							xwts=String.format(str, new Object[] { toFixed(value, 0) });
							if (value > 20) {
								xwtspy = "，媒体关注度较高";
							}
						}
					}
				}

				List<Map<String, Object>> stockTpList = (List<Map<String, Object>>) data.get("stockTp");
				if (null == stockTpList || stockTpList.size() == 0 || stockTpList.get(0) == null) {
					List<Map<String, Object>> ljrdTotalList = (List<Map<String, Object>>) data.get("ljrdTotal");
					if (null != ljrdTotalList && ljrdTotalList.size() > 0) {
						Integer mqTotal = ljrdTotalList.get(0).get("MQTotal") != null
								? ((BigDecimal) ljrdTotalList.get(0).get("MQTotal")).intValue()
								: 0;
						if (mqTotal > 0 && mqTotal <= 50) {
							ljrdTotal = "短线观望";
						} else if (mqTotal > 50 && mqTotal <= 80) {
							ljrdTotal = "可适当关注";
						} else if (mqTotal > 80) {
							ljrdTotal = "短线看多";
						}
					}
				} else {
					ljrdTotal ="今日停牌";
				}

				// chanceType
				String dqplpy = "";
				List<Map<String, Object>> chanceTypeList = (List<Map<String, Object>>) data.get("chanceType");
				if (null != chanceTypeList && chanceTypeList.get(0) != null) {
					String todayStr = localDateFormat.get().format(new Date());
					String[] weekDay = new String[] { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
					for (int i = 0; i < chanceTypeList.size(); i++) {
						if (chanceTypeList.get(i).get("dataType").equals("yypl")) {
							String recordDate = chanceTypeList.get(i).get("RecordDate").toString();
							int diff = dayDiff(recordDate, todayStr);
							if (diff <= 3) {
								try {
									int day = localDateFormat.get().parse(recordDate).getDay();
									dqplpy = "，" + weekDay[day] + "披露财务报告";
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

				String clsxg = "";
				List<Map<String, Object>> historyHighestList = (List<Map<String, Object>>) data.get("historyHighest");
				if (null != historyHighestList && historyHighestList.get(0) != null) {
					Integer sjc = (Integer) historyHighestList.get(0).get("sjc");
					if (sjc > 0) {
						clsxg = historyHighestList.get(0).get("TradingDay") + "日创历史新高，";
					}

				}

				String cdiffirentPrexg = "";
				List<Map<String, Object>> periodCxgList = (List<Map<String, Object>>) data.get("periodCxg");
				if (null != periodCxgList && periodCxgList.get(0) != null) {
					Map<String, Object> map = periodCxgList.get(0);
					Double closePrice = map.get("ClosePrice") == null ? 0d
							: ((BigDecimal) map.get("ClosePrice")).doubleValue();
					Double closePrice6M = map.get("ClosePrice6M") == null ? 0d
							: ((BigDecimal) map.get("ClosePrice6M")).doubleValue();
					Double closePrice3M = map.get("ClosePrice3M") == null ? 0d
							: ((BigDecimal) map.get("ClosePrice3M")).doubleValue();
					Double closePriceM = map.get("ClosePriceM") == null ? 0d
							: ((BigDecimal) map.get("ClosePriceM")).doubleValue();
					String tradingDay = map.get("TradingDay").toString();
					if (closePrice >= closePrice6M) {
						cdiffirentPrexg = tradingDay + "日创最近半年新高，";
					} else if (closePrice >= closePrice3M) {
						cdiffirentPrexg = tradingDay + "日创最近一季度新高，";
					} else if (closePrice >= closePriceM) {
						cdiffirentPrexg = tradingDay + "日创最近一月新高，";
					}
				}

				if (clsxg != "") {
					cxg = "";
					cdiffirentPrexg = "";
				} else if (clsxg == "" && cdiffirentPrexg != "") {
					cxg = "";
				}
				String dqpy = "近一月" + scgzd + jgdy + xwts + clsxg + cdiffirentPrexg + cxg + mce + mhyce + ljrdTotal + dqplpy;
				//TODO 短期评语
				System.out.println("短期评语： "+dqpy);
				
				finalResultMap.put("dqpy", dqpy);
				// ============================================================================短期评语

				String ltgbpy = "";
				if (ltgbList != null) {
					Long af = ltgbList.get(0).get("AFloatListed") == null ? 0l
							: ((Long) ltgbList.get(0).get("AFloatListed"));
					if (af > 0 && af < 300000000) {
						ltgbpy = "小盘";
					} else if (af >= 300000000 && af < 1000000000) {
						ltgbpy = "中盘";
					} else if (af >= 1000000000) {
						ltgbpy = "大盘";
					}
				}

				// quoteGj
				String gjhfpy = "";
				List<Map<String, Object>> quoteGjList = (List<Map<String, Object>>) data.get("quoteGj");
				if (quoteGjList != null) {
					int len = quoteGjList.size();
					Double closePrice = getDoubleField(quoteGjList.get(len - 1), "ClosePrice");
					if (closePrice > 0 && closePrice <= 10) {
						gjhfpy = "低价股，";
					} else if (closePrice > 10 && closePrice <= 50) {
						gjhfpy = "中价股，";
					} else if (closePrice > 50) {
						gjhfpy = "高价股，";
					}
				}

				// cwjkQscPm
				String cwjkfspy = "";
				List<Map<String, Object>> cwjkQscPmList = (List<Map<String, Object>>) data.get("cwjkQscPm");
				if (null != cwjkQscPmList && null != cwjkQscPmList.get(0)) {
					Map<String, Object> map = cwjkQscPmList.get(0);
					Integer value = getIntegerField(map, "MarketRank") / getIntegerField(map, "TotalNum");
					if (value >= 0 && value <= 0.4) {
						cwjkfspy = "综合财务状况较差";
					} else if (value > 0.4 && value <= 0.7) {
						cwjkfspy = "综合财务状况一般";
					} else {
						cwjkfspy = "综合财务状况良好";
					}
				}

				// finJzcsy
				String jzc = "";
				List<Map<String, Object>> finJzcsyList = (List<Map<String, Object>>) data.get("finJzcsy");
				if (null != finJzcsyList && finJzcsyList.get(0) != null) {
					Double value = getDoubleField(finJzcsyList.get(0), "Value");
					Double avgData = getDoubleField(finJzcsyList.get(0), "AvgData");
					if (value < avgData) {
						jzc = "，净资产收益率低于行业平均水平";
					} else if (value.doubleValue() == avgData.doubleValue()) {
						jzc = "，净资产收益率与行业平均水平持平";
					} else {
						jzc = "，净资产收益率高于行业平均水平";
					}
				}

				// finMll
				String mll = "";
				List<Map<String, Object>> finMllList = (List<Map<String, Object>>) data.get("finMll");
				if (null != finMllList && finMllList.get(0) != null) {
					Double value = getDoubleField(finMllList.get(0), "Value");
					Double avgData = getDoubleField(finMllList.get(0), "AvgData");

					if (value < avgData) {
						mll = "，毛利率低于行业平均水平";
					} else if (value.doubleValue() == avgData.doubleValue()) {
						mll = "，毛利率与行业平均水平持平";
					} else {
						mll = "，毛利率高于行业平均水平";
					}
				}

				// hysylRank
				String hysylpmpy = "";
				String estimateStr ="";
				List<Map<String, Object>> hysylRankList = (List<Map<String, Object>>) data.get("hysylRank");
				if (null != hysylRankList && hysylRankList.get(0) != null) {
					String[] arr = hysylRankList.get(0).get("Rank").toString().split("/");
					Double[] numArr = new Double[arr.length];
					for (int i = 0; i < arr.length; i++) {
						numArr[i] = Double.valueOf(arr[i]);
					}
					Double value = (numArr[1] - numArr[0]) / numArr[1] * 1.d;
					String industryName = zhpsfsList.get(0).get("SecondIndustryName").toString();
					if (value > 0 && value <= 0.3) {
						hysylpmpy = "，在" + industryName + "行业中估值偏低，有一定的上升空间";
						estimateStr +="，公司当前的市场估值被低估，有一定的上升空间；";
					} else if (value > 0.3 && value <= 0.7) {
						hysylpmpy = "，在" + industryName + "行业中估值合理";
						estimateStr +="，公司当前的市场估值合理；";
					} else {
						hysylpmpy = "，在" + industryName + "行业中估值偏高";
						estimateStr +="，公司当前的市场估值偏高；";
					}

				}

				// gzcwTotal
				String gzcwTotalpy = "";
				List<Map<String, Object>> gzcwTotalList = (List<Map<String, Object>>) data.get("gzcwTotal");
				if (null != gzcwTotalList && gzcwTotalList.get(0) != null) {
					Double value = getDoubleField(gzcwTotalList.get(0), "VFTotal");
					if (value > 0 && value <= 40) {
						gzcwTotalpy = "，继续观望";
					} else if (value > 40 && value <= 80) {
						gzcwTotalpy = "，可适当关注";
					} else if (value > 80 && value <= 100) {
						gzcwTotalpy = "，中长期看好";
					}
				}

				String zcqpy = "该股为" + ltgbpy + gjhfpy + cwjkfspy + mll + jzc + hysylpmpy + gzcwTotalpy;
				//TODO 中长期评语
				System.out.println("中长期评语： "+zcqpy);
				finalResultMap.put("zcqpy", zcqpy);

				/* 财务健康评语 */
				String cwjkfs = "-";
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

				String jlrpy = "", zcfzlpy = "", mgxjlpy = "";
				List<Map<String, Object>> cfjkpyList = (List<Map<String, Object>>) data.get("cfjkpy");

				if (null != cfjkpyList && cfjkpyList.size() > 0) {
					Map<String, String[]> diffMap = new HashMap<String, String[]>();
					diffMap.put("jlrzz", new String[] { "，盈利优势较为明显", "，业绩快速增长", "，业绩小幅增长", "，业绩下滑" });
					diffMap.put("zcfzl", new String[] { "，没有净资产或资不抵债", "，资产负债水平适宜", "" });
					diffMap.put("mgxjl", new String[] { "，现金流充足", "，现金流不足" });
					int index = 0;
					for (Map<String, Object> map : cfjkpyList) {
						String factorType = (String) map.get("FactorType");
						Double value = map.get("Value") == null ? null : ((BigDecimal) map.get("Value")).doubleValue();
						String[] diffArr = diffMap.get(factorType);
						if (null == diffArr) {
							continue;
						}
						if (factorType.equals("jlrzz")) {
							if (value >= 100) {
								index = 0;
							} else if (value >= 30 && value < 100) {
								index = 1;
							} else if (value >= 0 && value < 30) {
								index = 2;
							} else if (value < 0) {
								index = 3;
							}
							jlrpy = diffArr[index];

						} else if (factorType.equals("zcfzl")) {
							if (value >= 100) {
								index = 0;
							} else if (value >= 40 && value < 60) {
								index = 1;
							} else {
								index = 2;
							}
							zcfzlpy = diffArr[index];
						} else if (factorType.equals("mgxjl")) {
							if (value > 0) {
								index = 0;
							} else {
								index = 1;
							}
							mgxjlpy = diffArr[index];
						}
					}
				}

				// chanceType
				String chancepy = "";// 新增了之前机会中的业绩大增和高送转
				List<Map<String, Object>> chancepyList = (List<Map<String, Object>>) data.get("chanceType");
				if (chancepyList != null && chancepyList.get(0) != null) {
					for (Map<String, Object> map : chancepyList) {
						String dataType = (String) map.get("dataType");
						if (dataType.equals("yjdz")) {
							chancepy = "，业绩大增：" + map.get("InfoTitle");
						} else if (dataType.equals("gsz")) {
							chancepy = "，分红送转：" + map.get("InfoTitle");
						} else if (dataType.equals("yypl")) {
							chancepy = "，" + map.get("InfoTitle");
						} else {
							chancepy = "";
						}
					}
				}

				String cwjkpy = cwjkfspy + jlrpy + zcfzlpy + mgxjlpy + chancepy;
				//TODO 财务健康评语
				System.out.println("财务健康评语："+cwjkpy);
				
				finalResultMap.put("cwjkpy", cwjkpy);

				/* 价值评估评语 */
				String jzpgfs = "-";
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
				
				finalResultMap.put("jzpgfs", jzpgfs);

				String jzpgzfpd = "";
				String jzpgRank = "-";
				List<Map<String, Object>> jzpgRankList = (List<Map<String, Object>>) data.get("jzpgRank");
				if (null != jzpgRankList && null != jzpgRankList.get(0)) {
					Object ed = jzpgRankList.get(0).get("EndDate");
					if (null != ed && StringUtils.isNotBlank(ed.toString())) {
						jzpgRank = jzpgRankList.get(0).get("Rank") == null ? "-"
								: (String) jzpgRankList.get(0).get("Rank");
						if (!jzpgRank.equals("-")) {
							String[] rankArr = jzpgRank.split("/");
							Double[] numArr = new Double[rankArr.length];
							for (int i = 0; i < rankArr.length; i++) {
								numArr[i] = Double.valueOf(rankArr[i]);
							}
							if ((numArr[1] - numArr[0] + 1) / numArr[1] >= 0
									&& (numArr[1] - numArr[0] + 1) / numArr[1] < 0.5) {
								jzpgzfpd = "综合价值相对被高估，股价对其增长预期已有一定的反应，后市超预期增长的可持续性值得考量";
							} else {
								jzpgzfpd = "综合价值相对被低估，有一定的上升空间";
							}
						}
					}
				}


				String hyqzms = "";
				String industryName = "";
				if (null != hysylRankList && hysylRankList.get(0) != null) {
					hyqzms = "在" + zhpsfsList.get(0).get("SecondIndustryName") + "中";
					industryName=zhpsfsList.get(0).get("SecondIndustryName").toString();
				}
				
				String compoundAnnounce = "该股属于"+industryName+"行业，热门概念为"+hotConcept+",公司"+ cwjkfspy + estimateStr 
						+ "近一月" + xwts + clsxg + cdiffirentPrexg + cxg +scgzd2 +fundStr+  mce + mhyce + ljrdTotal + dqplpy;
				System.err.println(compoundAnnounce);
				
				

				String hykqpmpy = "", hykhpmpy = "", khpj = "", kqpj = "";
				// jzpgJxxPm
				List<String> reskqarray = new ArrayList<>();
				List<String> reskharray = new ArrayList<>();
				List<Map<String, Object>> jzpgJxxPmList = (List<Map<String, Object>>) data.get("jzpgJxxPm");
				if (jzpgJxxPmList != null && jzpgJxxPmList.get(0) != null) {
					Double rankSjl = Double.valueOf(getIntegerField(jzpgJxxPmList.get(0), "rankSjl"));
					Double hySjlTotalNum = Double.valueOf(getIntegerField(jzpgJxxPmList.get(0), "HySjlTotalNum"));
					Double rankSxl = Double.valueOf(getIntegerField(jzpgJxxPmList.get(0), "rankSxl"));
					Double rankSxxl = Double.valueOf(getIntegerField(jzpgJxxPmList.get(0), "rankSxxl"));

					Double sjlzb = rankSjl / hySjlTotalNum;
					Double sxlzb = rankSxl / hySjlTotalNum;
					Double sxxlzb = rankSxxl / hySjlTotalNum;

					if (sjlzb >= 0 && sjlzb < 0.5) {
						reskqarray.add("市净率");
					} else {
						reskharray.add("市净率");
					}
					if (sxlzb >= 0 && sxlzb < 0.5) {
						reskqarray.add("市销率");
					} else {
						reskharray.add("市销率");
					}
					if (sxxlzb >= 0 && sxxlzb < 0.5) {
						reskqarray.add("市现率");
					} else {
						reskharray.add("市现率");
					}

				}

				// hysylRank
				if (hysylRankList != null && hysylRankList.get(0) != null) {
					String[] rankArr = hysylRankList.get(0).get("Rank").toString().split("/");
					Double[] numArr = new Double[rankArr.length];
					for (int i = 0; i < rankArr.length; i++) {
						numArr[i] = Double.valueOf(rankArr[i]);
					}
					Double hysylpmzb = (numArr[1] - numArr[0] + 1) / numArr[1];
					if (hysylpmzb > 0 && hysylpmzb <= 0.5) {
						reskqarray.add("市盈率");
					} else {
						reskharray.add("市盈率");
					}
				}

				if (reskqarray.size() > 0) {
					for (int i = 0; i < reskqarray.size(); i++) {
						kqpj += reskqarray.get(i) + "、";
					}
				}
				if (reskharray.size() > 0) {
					for (int i = 0; i < reskharray.size(); i++) {
						khpj += reskharray.get(i) + "、";
					}
				}

				if (!kqpj.equals("")) {
					hykqpmpy = kqpj.substring(0, kqpj.length() - 1) + "等在行业中排名靠前";
				}
				if (!khpj.equals("")) {
					hykhpmpy = "，" + khpj.substring(0, khpj.length() - 1) + "等在行业中排名靠后";
				}

				String jzpgpy = hyqzms + jzpgzfpd + "，其中" + hykqpmpy + hykhpmpy;
				//TODO 价值评估评语
				System.out.println("价值评估评语："+jzpgpy);
				
				finalResultMap.put("jzpgpy", jzpgpy);
				
				//==============================================================行情表现评
				/*行情表现评语*/
				String mcepy="",lbpy = "",hspy="",hqbxpy="";				
				if(null != hqbxpyList) {
					for (Map<String, Object> map : hqbxpyList) {
						String factorType = (String) map.get("FactorType");
						Double value = map.get("Value") == null ? null : ((BigDecimal) map.get("Value")).doubleValue();
						if(factorType.equals("mce")) {
							mcepy=value>=0?"强于大盘":"弱于大盘";
						}else if(factorType.equals("mce")) {
							if(value>0 && value<1) {
								lbpy ="，成交量缩小";
							}else if(value>1 && value<3) {
								lbpy ="，成交量放大";
							}else if(value>3) {
								lbpy ="，成交量显著放大";
							}
						}else if(factorType.equals("hs")) {
							if(value>0 && value<1) {
								hspy ="，活跃度较小";
							}else if(value>1 && value<3) {
								hspy ="，活跃度一般";
							}else if(value>3) {
								hspy ="，活跃度较高";
							}
						}
					}
				}
				
				if(null != stockTpList && null != stockTpList.get(0)) {
					String[] arr = stockTpList.get(0).get("SuspendDate").toString().split("/");
					Double[] tpdate = convertDoubleArr(arr);
					Integer haltDay = (Integer) stockTpList.get(0).get("HaltDay");
					if(haltDay>=30) {
						hqbxpy ="该股从" + tpdate[0] + "年" + tpdate[1] + "月" + tpdate[2] + "日开始停牌";
					}else {
						hqbxpy = "近一个月走势" + mcepy + lbpy + hspy + lhbcspy + lhbjlrpy + zjlrzbpy;
					}
				}else {
					hqbxpy = "近一个月走势" + mcepy + lbpy + hspy + lhbcspy + lhbjlrpy + zjlrzbpy;
				}
				
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
				finalResultMap.put("hqbxpy", hqbxpy);
				//TODO 行情表现评语
				System.out.println("行情表现评语:"+hqbxpy);
				
				
				//==============================================市场热度评语=======================
				String ybslpy="",pjtgpy = "",zxpjpy = "",mbjszpy = "",fmxwpy = "",ggzcpy = "";
				if(null != scrdpyList) {
					for (Map<String, Object> map : scrdpyList) {
						String factorType = map.get("FactorType").toString();
						Double value = map.get("Value") == null ? null : ((BigDecimal) map.get("Value")).doubleValue();
						if(factorType.equals("ybsl")) {
							ybslpy = "，" +toFixed(value, 0) + "个机构撰写过该股的研究报告";
						}else if(factorType.equals("pjtg")) {
							pjtgpy = value ==0?"，无调高评级":"，" + toFixed(value, 0) + "篇调高评级";
						}else if(factorType.equals("zxpj")) {
							zxpjpy = "，最新评级" + map.get("MS");
						}else if(factorType.equals("mbjsz")) {
							mbjszpy = value<0?"，目标价小于最新价，下跌空间" + toFixed(Math.abs(value) * 100, 0) + "%":"，目标价大于最新价，上涨空间" + toFixed(Math.abs(value) * 100, 0) + "%";
						}else if(factorType.equals("ggzc")) {
							ggzcpy=value>=0.5?".近一月高管增持占流通股比例" + toFixed(value,0) + "%":"";
						}else if(factorType.equals("fmxw")) {
							fmxwpy = "，负面新闻" + toFixed(value,0) + "篇。";
						}
						
					}
				}
				
				String scrdpy ="";
				if(ybslpy.equals("") && xwtspy.equals("")) {
					scrdpy = "近3个月，该股未受到机构特别关注";
				}else {
					scrdpy = "近3个月" + ybslpy + pjtgpy + zxpjpy + mbjszpy + xwtspy + ggzcpy + fmxwpy;
				}

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
				finalResultMap.put("scrdpy", scrdpy);
				//TODO 市场热度评语
				System.out.println("市场热度评语："+ scrdpy);
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
		System.out.println("文件是否存在：" + file.exists());
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

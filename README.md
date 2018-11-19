# InvoiceSDK
基于词向量的编码—解码模型计算发票明细匹配度

主要负责基于海量企业历年进项和销项发票明细数据进行机器学习其潜在关联规则。

- 基于正则匹配和TextRank算法结构化提取关键词
- 使用基于双向RNN和Attention模型训练多串关键词对，获得潜在关联度
- 并基于Word2Vec训练学习关键词相似性匹配计算相似度，得到进项发票内容的真实合理度
- 加入已查出虚开的发票不断迭代训练计算获得虚开风险预警值，提供web API给平台调用

结合实际数据采用上述技术在基于通用和发票场景语料库中训练学习，给公司的企业报告平台提供企业进项发票虚开风险预警服务，已上线使用。

（P.S:训练数据涉及隐私，不便公开。）

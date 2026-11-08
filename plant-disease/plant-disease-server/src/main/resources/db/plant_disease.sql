
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `disease_control`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disease_control` (
  `id` int NOT NULL AUTO_INCREMENT,
  `disease_id` int NOT NULL COMMENT '对应病害ID',
  `symptom` text COMMENT '发病特征',
  `cause` text COMMENT '诱因',
  `pesticide` text COMMENT '防治药剂',
  `farming` text COMMENT '农事操作建议',
  PRIMARY KEY (`id`),
  KEY `disease_id` (`disease_id`),
  CONSTRAINT `disease_control_ibfk_1` FOREIGN KEY (`disease_id`) REFERENCES `disease_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病害防治知识库';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `disease_control` WRITE;
/*!40000 ALTER TABLE `disease_control` DISABLE KEYS */;
INSERT INTO `disease_control` VALUES (1,1,'叶片出现暗绿色圆形病斑，后期病斑变黑凹陷，带放射状黑色霉层','由黑星病菌引起，高湿低温、多雨多雾环境易爆发','可选用苯醚甲环唑、戊唑醇、代森锰锌','及时清除病叶病果，减少果园积水；春季萌芽前清园'),(2,2,'叶片表面出现白色粉状霉层，叶片卷曲褪绿、长势衰弱','白粉病菌侵染，温暖干燥、通风差时易扩散','选用三唑酮、腈菌唑、吡唑醚菌酯','避免偏施氮肥，增施磷钾肥；保持树冠通风透光'),(3,3,'叶片出现橙黄色圆形斑点，斑点中央带有黑色小点，后期叶片脱落','由桧柏等转主寄主传播的锈菌侵染，春季多雨容易流行','喷施三唑酮、戊唑醇、氟硅唑','果园周边避免种植桧柏类转主植物；雨后及时排水'),(4,4,'植株生长正常，无明显病斑，叶片翠绿舒展','健康植株，无病原菌侵染','无需用药','维持正常水肥管理，定期巡查预防病害'),(5,5,'叶片浓绿舒展，长势良好，无病斑','健康植株','无需用药','控制土壤酸性，保持湿润不积水，合理修剪'),(6,6,'叶片布满白色粉状物，叶片皱缩、焦枯','白粉病菌，高温高湿、通风不良发病重','腈菌唑、醚菌酯、戊唑醇','及时摘除病叶，合理修剪，降低田间湿度'),(7,7,'叶片正常翠绿，无病害特征','健康植株','无需用药','花期做好防冻，均衡施肥，提高树势抗病力'),(8,8,'叶片出现浅褐色椭圆形小点，逐渐扩大成灰褐色斑块，有黄褐色轮纹','尾孢真菌，高温高湿、连作地块易发','苯醚甲环唑、丙环唑、代森锰锌','实行轮作，及时摘除底部老病叶，合理密植'),(9,9,'叶片出现橘红色锈状小突起，后期表皮破裂散出锈粉','玉米锈病菌，温暖湿润、多雨天气快速传播','三唑酮、戊唑醇、氟环唑','选用抗病品种；雨后及时排水，降低田间湿度'),(10,10,'叶片出现灰褐色梭形大病斑，病斑有深色轮纹，严重叶片枯死','大斑病菌，20~25℃高湿环境爆发','代森锰锌、肟菌酯、丙环唑','轮作倒茬；收获后清除秸秆残体，减少菌源'),(11,11,'叶片正常，长势健壮','健康植株','无需用药','配方施肥，控氮增钾，增强植株抗性'),(12,12,'叶片出现红褐色圆形病斑，后期病斑扩大发黑，果实腐烂发黑','黑腐病菌，潮湿多雨、伤口容易侵染','苯醚甲环唑、嘧菌酯、代森锌','及时修剪病枝，清理病果，做好果园排水'),(13,13,'叶片出现红褐色斑点，叶脉坏死，果面出现黑褐色斑纹','葡萄埃斯卡真菌，树势弱、伤口侵染','嘧菌酯、咯菌腈，重点预防','避免枝干伤口，合理负载，增强树势'),(14,14,'叶缘开始出现黄褐色灼烧状病斑，向内扩展，叶片干枯脱落','叶枯病菌，高温高湿、通风差发病快','多菌灵、代森锰锌、苯醚甲环唑','及时摘除病叶，棚内加强通风降湿'),(15,15,'叶片翠绿，生长正常','健康植株','无需用药','控制挂果量，雨季提前保护性预防'),(16,16,'叶片不对称黄化，叶脉间斑驳黄，叶片硬化，果实畸形偏小','柑橘黄龙病，木虱传播，细菌性病害','无治疗药剂，重点杀木虱：噻虫嗪、联苯菊酯','及时挖除病树；全程防控木虱，培育无病苗'),(17,17,'叶片出现水渍状褐色小斑点，斑点穿孔脱落，形成孔洞','细菌性穿孔病菌，多雨、大风伤口侵染','春雷霉素、噻唑锌、氢氧化铜','春季清园；避免大水漫灌，减少叶面结露'),(18,18,'叶片舒展翠绿，无病斑','健康植株','无需用药','秋冬季基肥充足，雨季做好排水防涝'),(19,19,'叶片出现水渍状褐色斑点，后期斑点坏死穿孔，落叶','细菌性斑点病菌，高温高湿、雨水飞溅传播','春雷霉素、噻唑锌、中生菌素','轮作；定植无病壮苗，避免傍晚浇水'),(20,20,'叶片正常，植株健壮','健康植株','无需用药','均衡水肥，避免田间积水，通风控湿'),(21,21,'叶片出现褐色同心轮纹状病斑，逐步扩大，叶片枯黄脱落','早疫病菌侵染，高温干旱交替环境易发病','喷施代森锰锌、苯醚甲环唑、嘧菌酯','选用无病种薯；实行轮作；及时摘除病叶'),(22,22,'叶片出现水渍状暗绿色病斑，边缘白霉，快速蔓延，薯块腐烂','晚疫病菌，低温高湿、持续阴雨爆发','甲霜灵锰锌、烯酰吗啉、氟啶胺','避免大水漫灌，采用滴灌；发现中心病株立刻拔除'),(23,23,'叶片正常，长势良好','健康植株','无需用药','种薯消毒，起垄栽培，雨季排涝'),(24,24,'叶片翠绿无病斑','健康植株','无需用药','保持土壤疏松，及时除草，防积水'),(25,25,'叶片正常，生长健壮','健康植株','无需用药','合理轮作，控氮肥，防止徒长'),(26,26,'叶面布满白色粉霉，叶片发黄发脆、萎蔫','南瓜白粉菌，温暖干燥通风差爆发','醚菌酯、腈菌唑、吡唑醚菌酯','及时摘除底部老叶，加大通风，少浇叶面水'),(27,27,'叶缘出现灼烧状红褐色焦斑，向内扩展，叶片干枯卷曲','草莓叶枯病菌，高湿、低温环境发病','代森锰锌、苯醚甲环唑、嘧菌酯','及时摘除病叶；棚内通风降湿，避免积水'),(28,28,'叶片正常翠绿','健康植株','无需用药','地膜覆盖，避免泥土溅到叶片'),(29,29,'叶片出现褐色同心轮纹斑，从底部老叶向上发展','番茄早疫病菌，高温高湿、连作重茬易发','代森锰锌、百菌清、苯醚甲环唑','实行轮作倒茬；及时摘除下部病老叶'),(30,30,'叶片暗绿水渍状大病斑，潮湿时长白霉，快速枯萎','番茄晚疫病菌，低温阴雨、棚内结露爆发','甲霜灵锰锌、霜脲氰、氟啶胺','棚内早晚通风，减少叶面结露；隔离病株'),(31,31,'叶片正面出现淡黄色褪绿黄斑，背面长灰色霉层','叶霉病菌，棚内高湿、密闭不透风','多抗霉素、嘧霉胺、戊唑醇','加强棚室通风排湿；及时摘除发病叶片'),(32,32,'叶片出现圆形或近圆形褐色小点，后期斑点密集，叶片发黄脱落','壳针孢菌侵染，多雨多露、通风差发病','苯醚甲环唑、戊唑醇、百菌清','清洁田园，减少越冬菌源；控制种植密度'),(33,33,'叶片出现大量细碎黄白点，严重叶片灰白干枯，背面有小虫','二斑叶螨（红蜘蛛），高温干燥繁殖极快','阿维菌素、乙螨唑、螺螨酯','增加田间湿度；清除杂草，隔离虫源植株'),(34,34,'叶片出现灰褐色靶环状病斑，中间浅、边缘深褐色','靶斑病菌，高湿、温差大棚室高发','苯醚甲环唑、嘧菌酯、代森锰锌','合理通风，不要忽干忽湿，及时摘除病叶'),(35,35,'叶片向上卷曲、黄化，植株矮缩，新叶变小畸形','番茄黄曲叶病毒，烟粉虱传播','无特效药，防虫：噻虫嗪、吡虫啉','全程防治烟粉虱；拔除病株，清理杂草毒源'),(36,36,'叶片黄绿相间斑驳，皱缩畸形，植株矮化','番茄花叶病毒，接触、蚜虫传播','无特效药，预防蚜虫：吡虫啉、啶虫脒','农事操作先洗手；及时防蚜虫，拔除重病株'),(37,37,'叶片出现水渍状褐色小斑点，后期坏死穿孔，茎秆溃疡','细菌性斑点病菌，雨水飞溅、伤口侵染','春雷霉素、噻唑锌、氢氧化铜','选用抗病品种；避免喷灌，减少叶片沾水'),(38,38,'叶片舒展正常，长势健康','健康植株','无需用药','平衡水肥，通风透光，定期巡查预防'),(39,39,'无叶片，空白背景','无病害','无需用药','该类别为背景，不做病害识别结果');
/*!40000 ALTER TABLE `disease_control` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `disease_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disease_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '病害ID，与模型输出索引+1对应',
  `crop_name` varchar(50) NOT NULL COMMENT '作物名称',
  `disease_name` varchar(100) NOT NULL COMMENT '病害名称',
  `en_name` varchar(100) NOT NULL COMMENT '英文类别名(对应PlantVillage)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病害基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `disease_info` WRITE;
/*!40000 ALTER TABLE `disease_info` DISABLE KEYS */;
INSERT INTO `disease_info` VALUES (1,'苹果','黑星病','Apple___Apple_scab','2026-10-22 14:40:48'),(2,'苹果','白粉病','Apple___Powdery_mildew','2026-10-22 14:40:48'),(3,'苹果','锈病','Apple___Cedar_apple_rust','2026-10-22 14:40:48'),(4,'苹果','健康','Apple___healthy','2026-10-22 14:40:48'),(5,'蓝莓','健康','Blueberry___healthy','2026-10-22 14:40:48'),(6,'樱桃','白粉病','Cherry_(including_sour)___Powdery_mildew','2026-10-22 14:40:48'),(7,'樱桃','健康','Cherry_(including_sour)___healthy','2026-10-22 14:40:48'),(8,'玉米','灰叶斑病','Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot','2026-10-22 14:40:48'),(9,'玉米','普通锈病','Corn_(maize)___Common_rust_','2026-10-22 14:40:48'),(10,'玉米','大斑病','Corn_(maize)___Northern_Leaf_Blight','2026-10-22 14:40:48'),(11,'玉米','健康','Corn_(maize)___healthy','2026-10-22 14:40:48'),(12,'葡萄','黑腐病','Grape___Black_rot','2026-10-22 14:40:48'),(13,'葡萄','轮斑病','Grape___Esca_(Black_Measles)','2026-10-22 14:40:48'),(14,'葡萄','叶枯病','Grape___Leaf_blight_(Isariopsis_Leaf_Spot)','2026-10-22 14:40:48'),(15,'葡萄','健康','Grape___healthy','2026-10-22 14:40:48'),(16,'橙子','黄龙病','Orange___Haunglongbing_(Citrus_greening)','2026-10-22 14:40:48'),(17,'桃','细菌性穿孔病','Peach___Bacterial_spot','2026-10-22 14:40:48'),(18,'桃','健康','Peach___healthy','2026-10-22 14:40:48'),(19,'辣椒','细菌性斑点病','Pepper,_bell___Bacterial_spot','2026-10-22 14:40:48'),(20,'辣椒','健康','Pepper,_bell___healthy','2026-10-22 14:40:48'),(21,'土豆','早疫病','Potato___Early_blight','2026-10-22 14:40:48'),(22,'土豆','晚疫病','Potato___Late_blight','2026-10-22 14:40:48'),(23,'土豆','健康','Potato___healthy','2026-10-22 14:40:48'),(24,'覆盆子','健康','Raspberry___healthy','2026-10-22 14:40:48'),(25,'大豆','健康','Soybean___healthy','2026-10-22 14:40:48'),(26,'南瓜','白粉病','Squash___Powdery_mildew','2026-10-22 14:40:48'),(27,'草莓','叶枯病','Strawberry___Leaf_scorch','2026-10-22 14:40:48'),(28,'草莓','健康','Strawberry___healthy','2026-10-22 14:40:48'),(29,'番茄','早疫病','Tomato___Early_blight','2026-10-22 14:40:48'),(30,'番茄','晚疫病','Tomato___Late_blight','2026-10-22 14:40:48'),(31,'番茄','叶霉病','Tomato___Leaf_Mold','2026-10-22 14:40:48'),(32,'番茄','斑点病','Tomato___Septoria_leaf_spot','2026-10-22 14:40:48'),(33,'番茄','二斑叶螨','Tomato___Spider_mites Two-spotted_spider_mite','2026-10-22 14:40:48'),(34,'番茄','靶斑病','Tomato___Target_Spot','2026-10-22 14:40:48'),(35,'番茄','花叶病毒病','Tomato___Tomato_Yellow_Leaf_Curl_Virus','2026-10-22 14:40:48'),(36,'番茄','黄曲叶病毒病','Tomato___Tomato_mosaic_virus','2026-10-22 14:40:48'),(37,'番茄','细菌性溃疡斑','Tomato___Bacterial_spot','2026-10-22 14:40:48'),(38,'番茄','健康','Tomato___healthy','2026-10-22 14:40:48'),(39,'背景无叶片','背景无叶片','Background_without_leaves','2026-10-22 14:40:48');
/*!40000 ALTER TABLE `disease_info` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `recognize_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recognize_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `image_url` varchar(255) NOT NULL COMMENT '图片路径',
  `disease_id` int NOT NULL COMMENT '识别病害ID',
  `confidence` decimal(5,4) NOT NULL COMMENT '置信度',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `recognize_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='识别历史记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `recognize_record` WRITE;
/*!40000 ALTER TABLE `recognize_record` DISABLE KEYS */;
INSERT INTO `recognize_record` VALUES (40,7,'/upload/b69c8661-626a-4155-be90-5cb5239abdf7.jpg',8,1.0000,'2026-11-06 13:25:50');
/*!40000 ALTER TABLE `recognize_record` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(MD5加密)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- 演示账号：admin / 123456（BCrypt）
INSERT INTO `sys_user` (`username`, `password`) VALUES ('admin', '$2a$10$HaaUUMQbTRxX3C6jm3/RAugHhAFnvNXRGMz1oaVvFr8P11q/7GUNG');

-- 补充模型类别 Apple___Black_rot（class_names.txt 中存在，原数据缺失）
INSERT INTO `disease_info` (`crop_name`, `disease_name`, `en_name`)
SELECT '苹果', '黑腐病', 'Apple___Black_rot'
WHERE NOT EXISTS (SELECT 1 FROM `disease_info` WHERE `en_name` = 'Apple___Black_rot');

INSERT INTO `disease_control` (`disease_id`, `symptom`, `cause`, `pesticide`, `farming`)
SELECT di.id,
       '叶片出现紫褐色至黑色病斑，果实表面产生同心轮纹状腐烂，后期病果干缩。',
       '由苹果黑腐病菌引起，高温高湿、虫伤或机械伤口多时容易侵染。',
       '可选用甲基硫菌灵、代森锰锌、苯醚甲环唑等药剂。',
       '及时清除病果病叶，修剪改善通风透光，避免果实机械损伤。'
FROM `disease_info` di
WHERE di.en_name = 'Apple___Black_rot'
  AND NOT EXISTS (
      SELECT 1 FROM `disease_control` dc WHERE dc.disease_id = di.id
  );

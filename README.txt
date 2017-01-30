Đứng ở thư mục jmdn-maxent/libs và thực hiện dòng lệnh sau để huấn luyện thử mô hình phân loại trong thư mục jmdn-maxent/models/sample

Linux:
java -classpath jmdn-maxent.jar:jmdn-base.jar:args4j-2.0.6.jar jmdn.method.classification.maxent.Trainer -all -d ../models/sample/

Windows:
java -classpath jmdn-maxent.jar;jmdn-base.jar;args4j-2.0.6.jar -Dfile.encoding=UTF-8 jmdn.method.classification.maxent.Trainer -all -d ../models/sample/
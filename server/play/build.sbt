name := """UniAppPlayServices"""

offline := true

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)


scalaVersion := "2.11.1"


resolvers +=Resolver.mavenLocal

libraryDependencies ++= Seq(
   javaJdbc,
   javaEbean,
   cache,
   javaWs,
   filters,
   "com.fasterxml.uuid"%"java-uuid-generator"%"3.1.4",
   "org.apache.commons" % "commons-math3" % "3.6.1",
   "org.springframework" % "spring-context" % "3.2.3.RELEASE",
   "org.springframework" % "spring-aop" % "3.2.3.RELEASE",
   "org.springframework" % "spring-expression" % "3.2.3.RELEASE",
   "org.springframework" % "spring-test" % "3.2.3.RELEASE",
   "org.springframework" % "spring-web" % "4.3.2.RELEASE",
   "com.typesafe.akka" %% "akka-actor" % "2.4.9",
   "commons-configuration" % "commons-configuration" % "1.10",
   "org.mindrot" % "jbcrypt" % "0.3m",
   "com.vassarlabs.proj.uniapp.launch"%"com-vassarlabs-proj-uniapp-launch"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.fileupload.api"%"com-vassarlabs-prod-fileupload-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.fileupload.impl"%"com-vassarlabs-prod-fileupload-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.fileupload.service"%"com-vassarlabs-prod-fileupload-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.restcall"%"com-vassarlabs-prod-restcall"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.spel"%"com-vassarlabs-prod-spel"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.computation.api"%"com-vassarlabs-proj-uniapp-computation-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.computation.impl"%"com-vassarlabs-proj-uniapp-computation-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.computation.service"%"com-vassarlabs-proj-uniapp-computation-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.dashboard.api"%"com-vassarlabs-proj-uniapp-dashboard-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.dashboard.impl"%"com-vassarlabs-proj-uniapp-dashboard-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.dashboard.service"%"com-vassarlabs-proj-uniapp-dashboard-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.fileupload.api"%"com-vassarlabs-proj-uniapp-fileupload-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.fileupload.impl"%"com-vassarlabs-proj-uniapp-fileupload-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.fileupload.service"%"com-vassarlabs-proj-uniapp-fileupload-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.common.filter.api"%"com-vassarlabs-prod-common-filter-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.common.filter.impl"%"com-vassarlabs-prod-common-filter-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.common.filter.service"%"com-vassarlabs-prod-common-filter-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.utils"%"com-vassarlabs-proj-uniapp-utils"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.app.api"%"com-vassarlabs-proj-uniapp-app-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.app.impl"%"com-vassarlabs-proj-uniapp-app-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.app.service"%"com-vassarlabs-proj-uniapp-app-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.common.utils"%"com-vassarlabs-prod-common-utils"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.prod.sms"%"com-vassarlabs-prod-sms"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.processor.api"%"com-vassarlabs-proj-uniapp-processor-api"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.processor.impl"%"com-vassarlabs-proj-uniapp-processor-impl"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.proj.uniapp.processor.service"%"com-vassarlabs-proj-uniapp-processor-service"%"0.0.1-SNAPSHOT",
   "com.vassarlabs.awss3"%"vassarlabs-aws-s3"%"1.0.0-SNAPSHOT",
   "org.imgscalr"%"imgscalr-lib"%"4.2",
   "org.jsoup" % "jsoup" % "1.7.2"
 )

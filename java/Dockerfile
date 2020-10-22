FROM tomcat:9-jre8-alpine

ADD target/test.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run", "-DREMOTE_ADDR=$REMOTE_ADDR"]
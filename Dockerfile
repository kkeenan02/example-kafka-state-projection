
ARG ARTIFACTORY_HOSTNAME
FROM <insert-base-here> as base

ARG ARTIFACTORY_HOSTNAME

COPY settings.xml /settings.xml

RUN curl "https://${ARTIFACTORY_HOSTNAME}/path-to-artifact/apache-maven-<version>/bin" | tar xvz -C /usr/local/src  && \
    yum install --nogpgcheck -y git  && \
    yum clean all && \
    rm -rf /var/cache/yum

ENV M2_HOME="/usr/local/src/apache-maven" PATH="/usr/local/src/apache-maven-<version>/bin:${PATH}:/maven-build/bin/"

WORKDIR /src

COPY . .

COPY spark-core-2.8.0.jar /usr/local/src/apache-maven/.m2/repository/

EXPOSE 8080

RUN mvn clean
RUN mvn -DskipTests install
# RUN mvn exec:java -Dexec.mainClass="com.kafka.app.App"

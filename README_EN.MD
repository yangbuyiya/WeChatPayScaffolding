# Play WeChat payment from zero

### Companion Articles

- [Chapter 1: Opening of Wechat Pay from Zero to Fun Series](https://www.yby6.com/archives/wechatpay02)
- [Chapter 2: Wechat Pay Security from Zero to Fun Series](https://www.yby6.com/archives/wechatpay01)
- [Chapter 3 From zero to fun series of WeChat payment actual combat basic framework] (https://www.yby6.com/archives/wechatpay03)
- [Chapter 4 From Zero to Fun Series: Wechat Payment Actual Combat PC Interface Construction](https://www.yby6.com/archives/wechat04)
- [Stay tuned....]

### introduce

Use WeChat Official Documents to Connect to WeChat Payment

The built-in vue3+vite front-end uses WeChat official native interface to dock with native v3 v2

The built-in applet uses the quick connection artifact IJPay to connect to jsapi v3

Synchronous update: code cloud [GITEE](https://gitee.com/yangbuyi/wxDemo) foreign [GITHUB](https://github.com/yangbuyiya/wxDemo)

### Software Architecture

<p> front end vue3 + vite + element plus
<p> applet uniapp
<p> Backend boot 2.x + mybatis plus + mysql

### Installation Tutorial

1. Copy all the merchant certificates to the **resources** directory
2. Download the WeChat platform certificate (not the merchant certificate)

#### 2.1 Certificate Downloader

Certificate Downloader is a command-line download tool for the Java WeChat Payment APIv3 platform certificate.

#### Quick Start

The tool has been packaged via Maven as
CertificateDownloader.jar, available for download in [release ](https://github.com/EliasZzz/CertificateDownloader/releases).
After downloading the jar package, if you do not have a certificate, the command to download the certificate for the first time is as follows

```bash
java -jar CertificateDownloader.jar -k ${apiV3key} -m ${mchId} -f ${mchPrivateKeyFilePath} -s ${mchSerialNo} -o ${outputFilePath}

Example:

java -jar CertificateDownloader-1.2.0-jar-with-dependencies.jar
-k=merchant v3key
-m=Merchant ID
-f=/Users/yangbuyi/Documents/workPath/workPath/wx-play-demo/src/main/resources/apiclient_key.pem
-s=merchant certificate serial number
-o=/Users/yangbuyi/Documents/workPath/workPath/wx-play-demo/src/main/resources

explain:

java -jar CertificateDownloader-1.2.0-jar-with-dependencies.jar
-k=This is the APIV3Key (wxpay.api-v3-key) merchant APIV3Key in the corresponding configuration file
-m=This is the mchID (wxpay.mch-id) merchant ID in the corresponding configuration file
-f=This is the merchant certificate apiclient_key.pem (absolute path address) in the corresponding configuration file
-s=This is the mchSerialNo (wxpay.mch-serial-no) merchant platform certificate serial number in the corresponding configuration file
-o=This is the directory under which the generated files will be output (absolute path address)

The required parameters are:

- `-f <privateKeyFilePath>`, Merchant API private key file path
- `-k <apiV3Key>`, key for certificate decryption
- `-m <merchantId>`, merchant ID
- `-o <outputFilePath>`, the path to save the certificate
- `-s <merchantSerialNo>`, the serial number of the merchant API certificate
```

![](https://foruda.gitee.com/images/1685674014076230916/f6659409_5151444.png)

### Backend configuration

<p> 1.1 Go to wxpay.properties and configure the corresponding WeChat configuration</p>
<p> 1.2 Go to application.yml and configure the database link information

### PC side configuration

<p> 1. vite.config.js configure your backend request url

### uniapp applet configuration

<p> 1.1 Use HBuilder software to import wx-play-uniapp
<p> 1.2 Modify the backend corresponding to appId
<p> 1.3 Modify wx-play-uniapp/utils/request.js request path baseUrl

### Local payment callback debugging

1. Use the intranet penetration tool to proxy the backend port
2. Modify the corresponding baseUrl in the previous paragraph

# WeChat group chat

Search `BN_Tang` on WeChat, add friends and note WeChat payment invitation to join the group answer

<img src="https://foruda.gitee.com/images/1685673733774119250/fe04bfeb_5151444.png" width="300px" height="450px" />

# PC demo

[Go to](https://lzys522.cn/wx/)

![Enter picture description](https://foruda.gitee.com/images/1685673897693356870/89f3440f_5151444.png)

# uniapp applet demo

<img src="https://foruda.gitee.com/images/1685673930068447278/c20a6a06_5151444.png" width="300px" height="450px" />
<img src="https://foruda.gitee.com/images/1685673948311564004/1354febc_5151444.png" width="300px" height="450px" />
<img src="https://foruda.gitee.com/images/1685673968161079699/b1465aba_5151444.png" width="300px" height="450px" />
<img src="https://foruda.gitee.com/images/1685673988191687152/69f3b19b_5151444.png" width="300px" height="450px" />

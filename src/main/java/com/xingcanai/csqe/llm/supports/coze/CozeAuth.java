package com.xingcanai.csqe.llm.supports.coze;

import java.io.IOException;
// import com.coze.openapi.client.auth.OAuthToken;
// import com.coze.openapi.service.auth.JWTOAuthClient;
import com.xingcanai.csqe.util.ResourceFileReader;


public class CozeAuth {

    // public static OAuthToken createToken(String appId, String publicKey, String privateKey) {
    //     JWTOAuthClient oauth;
    //     try {
    //         oauth = new JWTOAuthClient.JWTOAuthBuilder()
    //                     .clientID(appId)
    //                     .privateKey(privateKey)
    //                     .publicKey(publicKey)
    //                     .baseURL("https://api.coze.cn")
    //                     .build();
    //         return oauth.getAccessToken();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }

    // public static OAuthToken createTokenFromKeyPath(String appId, String publicKey, String privateKeyPath) {
    //     String privateKey;
    //     try {
    //         privateKey = ResourceFileReader.readResourceFile(privateKeyPath);
    //         return createToken(appId, publicKey, privateKey);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }

    // public static String createAccessTokenFromKeyPath(String appId, String publicKey, String privateKeyPath) {
    //     var token = createTokenFromKeyPath(appId, publicKey, privateKeyPath);
    //     return token != null ? token.getAccessToken() : "";
    // }

    // public static void main(String[] args) throws IOException {
    //     String privateKeyPath = "coze_private_key.pem";
    //     String publicKey = "DmkS_VOHG5F_BK8ISPXY2aGFEz6ddSZOa-1iXDcxdEg";
    //     var appId = "1185261263158";
    //     // var token = createToken(appId, publicKey, privateKey);
    //     // System.out.println(token);

    //     try {
    //         var token = createTokenFromKeyPath(appId, publicKey, privateKeyPath);
    //         System.out.println(token);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    // }
}

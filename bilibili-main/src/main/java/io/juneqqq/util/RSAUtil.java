package io.juneqqq.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA加密
 * 非对称加密，有公钥和私钥之分，公钥用于数据加密，私钥用于数据解密。加密结果可逆
 * 公钥一般提供给外部进行使用，私钥需要放置在服务器端保证安全性。
 * 特点：加密安全性很高，但是加密速度较慢
 *
 */
@SuppressWarnings("All")
public class RSAUtil {

	private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDAlatxcTN+ltismK6XTHgDui+mvsTOi27IwSbjIJHPqOJLCXaOvLoeHbVJpXYiEajP7moY60NCMon0yMFS9XsmB8lfpNziWCM3MRVb+93YHG3m23YOAtzMz8hiWoMA9FRFy4MTsEjhYww/wvdhUZbpZp3QnSrQL8aHy4WGZjwaPwIDAQAB";
	private static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMCVq3FxM36W2KyYrpdMeAO6L6a+xM6LbsjBJuMgkc+o4ksJdo68uh4dtUmldiIRqM/uahjrQ0IyifTIwVL1eyYHyV+k3OJYIzcxFVv73dgcbebbdg4C3MzPyGJagwD0VEXLgxOwSOFjDD/C92FRlulmndCdKtAvxofLhYZmPBo/AgMBAAECgYBftAEWxuKILf5PBzD8Dww1DflQK80xtyi+qv3gMPdE8vBBydoY5MC45pqIlqk2FQCHFDu1VnR+GQ9ljb5Es6kWRj73Lq3EkUQiftShKEJmJc+NqeLriUEqyjQOJap1ksvAfvti6KlvNfsCKlwqiwPzAwuenC3BkujDna85qJuAwQJBAMEK/jIwZABn+NqqzJsfH4YoGLoFX0BHwvs7wIyGxeA5jB1z5Rwg7arwGCR6FJxyy8a8XFCzU5yDcn5l4whBdhUCQQD/ZGoExuET7+kbT/XXEHuGcau4zAKdQU7pwBF5BkRH+R6WQjqOxtxEo0p6N/aQLWAJqXmFByGDpgLTW7KF/tgDAkBr48z2F/2MDJAVBmicOeTQghBvxxjZQEJFT7vpzllBXGhm/aMK+YxbgRy9Jk3msnIZfKTLpa4RR5Xx9tfQgWWRAkEAxUsJK8+HqSM47USEIjQ1eNLvWb8gdeMx1xntZZUVwpQMsP9QxWOSXePXcTsyWobzHgOyQLYVieIBZM39x83riQJBAJq4LuawrsnzPA6gyhXGavk3yU1NS2vf0uA4L2ChnfgKPk6uZjmkT3snW+GselRBm4WBny9M1+amPHyFzd8/QyU=";

	public static void main(String[] args) throws Exception{
		String str = RSAUtil.encrypt("111111");
		System.out.println(str);
		System.out.println(RSAUtil.decrypt(str));
	}

	public static String getPublicKeyStr(){
		return PUBLIC_KEY;
	}

	public static RSAPublicKey getPublicKey() throws Exception {
		byte[] decoded = Base64.decodeBase64(PUBLIC_KEY);
		return (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));
	}

	public static RSAPrivateKey getPrivateKey() throws Exception {
		byte[] decoded = Base64.decodeBase64(PRIVATE_KEY);
		return (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));
	}
	
	public static RSAKey generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(1024, new SecureRandom());
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
		String privateKeyString = new String(Base64.encodeBase64(privateKey.getEncoded()));
		return new RSAKey(privateKey, privateKeyString, publicKey, publicKeyString);
	}

	public static String encrypt(String source) throws Exception {
		byte[] decoded = Base64.decodeBase64(PUBLIC_KEY);
		RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(1, rsaPublicKey);
		return Base64.encodeBase64String(cipher.doFinal(source.getBytes(StandardCharsets.UTF_8)));
	}

	public static Cipher getCipher() throws Exception {
		byte[] decoded = Base64.decodeBase64(PRIVATE_KEY);
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(2, rsaPrivateKey);
		return cipher;
	}

	public static String decrypt(String text) throws Exception {
		Cipher cipher = getCipher();
		byte[] inputByte = Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8));
		return new String(cipher.doFinal(inputByte));
	}
	
	public static class RSAKey {
		  private RSAPrivateKey privateKey;
		  private String privateKeyString;
		  private RSAPublicKey publicKey;
		  public String publicKeyString;

		  public RSAKey(RSAPrivateKey privateKey, String privateKeyString, RSAPublicKey publicKey, String publicKeyString) {
		    this.privateKey = privateKey;
		    this.privateKeyString = privateKeyString;
		    this.publicKey = publicKey;
		    this.publicKeyString = publicKeyString;
		  }

		  public RSAPrivateKey getPrivateKey() {
		    return this.privateKey;
		  }

		  public void setPrivateKey(RSAPrivateKey privateKey) {
		    this.privateKey = privateKey;
		  }

		  public String getPrivateKeyString() {
		    return this.privateKeyString;
		  }

		  public void setPrivateKeyString(String privateKeyString) {
		    this.privateKeyString = privateKeyString;
		  }

		  public RSAPublicKey getPublicKey() {
		    return this.publicKey;
		  }

		  public void setPublicKey(RSAPublicKey publicKey) {
		    this.publicKey = publicKey;
		  }

		  public String getPublicKeyString() {
		    return this.publicKeyString;
		  }

		  public void setPublicKeyString(String publicKeyString) {
		    this.publicKeyString = publicKeyString;
		  }
		}
}
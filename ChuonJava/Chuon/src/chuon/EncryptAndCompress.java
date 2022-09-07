package chuon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptAndCompress 
{
    static public String GenerateAESKey()
    {
        return UUID.randomUUID().toString();
    }

    public static byte[] AESEncrypt(byte[] inputByteArray, byte[] IV, String strKey) throws Exception
    {
    	byte[] raw = strKey.getBytes();
    	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    	Cipher cipher = Cipher.getInstance("AES");//"算法/模式/补码方式"
    	IvParameterSpec iv = new IvParameterSpec(IV);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
    	cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
    	byte[] encrypted = cipher.doFinal(inputByteArray);    	
        return encrypted;
    }

    public static byte[] AESDecrypt(byte[] cipherText, byte[] IV, String strKey) throws Exception
    {
    	byte[] raw = strKey.getBytes();
    	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    	Cipher cipher = Cipher.getInstance("AES");
    	IvParameterSpec iv = new IvParameterSpec(IV);
    	cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
    	byte[] original = cipher.doFinal(cipherText);
        return original;
    }
    public class RSAKeyPair
    {
        String PrivateKey;
        String PublicKey;

        public String getPrivateKey() {
			return PrivateKey;
		}
        
        public String getPublicKey() {
			return PublicKey;
		}
        
        public byte[] getPrivateKeyBytes()
        {
            return Base64.getDecoder().decode(PrivateKey);
        }
        public byte[] getPublicKeyBytes()
        {
            return Base64.getDecoder().decode(PublicKey);
        }

        public RSAKeyPair(String PrivateKey, String PublicKey)
        {
            this.PrivateKey = PrivateKey;
            this.PublicKey = PublicKey;
        }

        public RSAKeyPair(String PublicKey)
        {
            this.PrivateKey = "";
            this.PublicKey = PublicKey;
        }

        public RSAKeyPair(byte[] PrivateKey, byte[] PublicKey)
        {
            this.PrivateKey = Base64.getEncoder().encodeToString(PrivateKey);
            this.PublicKey = Base64.getEncoder().encodeToString(PublicKey);
        }

        public RSAKeyPair(byte[] PublicKey)
        {
            this.PrivateKey = "";
            this.PublicKey = Base64.getEncoder().encodeToString(PublicKey);
        }
    }

    static public int GetRSAKeySize(String Key) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        try {
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(Key));
            RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(privateKeySpec);
            return privateKey.getModulus().bitLength();
		} catch (InvalidKeySpecException e) {
	    	X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(Key));
	        RSAPublicKey publicKey = (RSAPublicKey)keyFactory.generatePublic(keySpec);
	        return publicKey.getModulus().bitLength();
		}
    }

    static public RSAKeyPair GenerateRSAKeys(int size) throws Exception
    {
    	KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(size);
        KeyPair keyPair = keyPairGen.generateKeyPair();
    	
        return new EncryptAndCompress().new RSAKeyPair(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()), Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    }

    static private byte[] Xor(byte[] a, byte[] b)
    {
    	byte[] ans = new byte[a.length];
    	for (int i = 0; i < a.length; i++)
    	{
    		ans[i] = (byte) (a[i] ^ b[i]);
    	}
    	return ans;
    }
    
    static public byte[] RSAEncrypt(String publicKey, byte[] IV, byte[] content) throws Exception
    {   
    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    	X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        RSAPublicKey rsapublicKey = (RSAPublicKey)keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, rsapublicKey);
        
        int buffersize = (rsapublicKey.getModulus().bitCount() / 8) - 11;

        byte[] newIV = new byte[buffersize];
        System.arraycopy(IV, 0, newIV, 0, Math.min(buffersize, IV.length));

        for (int i = IV.length; i < buffersize; i++)
        {
            int now = (newIV[i - IV.length] + newIV[i - IV.length + 1]) % 256;
            newIV[i] = (byte)now;
        }

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
	        try (DataOutputStream writer = new DataOutputStream(stream))
	        {
	            for (int i = 0; i < content.length; i += buffersize)
	            {
	                int copyLength = Math.min(content.length - i, buffersize);
	                byte[] buffer = new byte[copyLength];
	                System.arraycopy(content, i, buffer, 0, copyLength);
	
	                byte[] nowIV = new byte[copyLength];
	                System.arraycopy(newIV, 0, nowIV, 0, copyLength);
	                byte[] encryptdata = cipher.doFinal(Xor(buffer, nowIV));
	                writer.write(encryptdata);
	
	                System.arraycopy(encryptdata, 0, newIV, 0, newIV.length);
	            }
	            writer.close();
	            stream.close();
	            return stream.toByteArray();
	        }
        }
    }

    static public byte[] RSADecrypt(String privateKey, byte[] IV, byte[] encryptedContent) throws Exception
    {
    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    	PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);

        int buffersize = rsaPrivateKey.getModulus().bitCount() / 8;
        int Decryptsize = (rsaPrivateKey.getModulus().bitCount() / 8) - 11;

        byte[] newIV = new byte[Decryptsize];
        System.arraycopy(IV, 0, newIV, 0, Math.min(Decryptsize, IV.length));

        for (int i = IV.length; i < Decryptsize; i++)
        {
            int now = (newIV[i - IV.length] + newIV[i - IV.length + 1]) % 256;
            newIV[i] = (byte)now;
        }

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
	        try (DataOutputStream writer = new DataOutputStream(stream))
	        {
	            for (int i = 0; i < encryptedContent.length; i += buffersize)
	            {
	                int copyLength = Math.min(encryptedContent.length - i, buffersize);
	                byte[] buffer = new byte[copyLength];
	                System.arraycopy(encryptedContent, i, buffer, 0, copyLength);
	
	                byte[] decryptString = cipher.doFinal(buffer);
	
	                byte[] nowIV = new byte[decryptString.length];
	                System.arraycopy(newIV, 0, nowIV, 0, decryptString.length);
	
	                writer.write(Xor(decryptString, nowIV));
	
	                System.arraycopy(buffer, 0, newIV, 0, newIV.length);
	            }
	            writer.close();
	            stream.close();
	            return stream.toByteArray();
	        }
        }
    }

    static public byte[] RSASignData(String privateKey, byte[] content, Signature signature ) throws Exception
    {
    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    	PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);

    	signature.initSign(rsaPrivateKey);
    	signature.update(content);

        return signature.sign();
    }

    static public boolean RSAVerifyData(String publicKey, byte[] content, byte[] usesignature, Signature signature) throws Exception
    {
    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    	X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        RSAPublicKey rsapublicKey = (RSAPublicKey)keyFactory.generatePublic(keySpec);

    	signature.initVerify(rsapublicKey);
    	signature.update(content);

        return signature.verify(usesignature);
    }

    public enum LockType
    {
        None,
        AES,
        RSA,
    }

    static public byte[] Lock(byte[] bs, String key, LockType _Lock) throws Exception
    {
        byte[] encryptBytes = null;

        Func<Object, byte[]> setdata = new Func<Object, byte[]>() {
			
			@Override
			public byte[] run(Object... args) throws Exception {
	            try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
	            {
		            try (DataOutputStream writer = new DataOutputStream(stream))
		            {
		                writer.writeByte((int)((LockType)args[0]).ordinal());
		                if ((byte[])args[1] != null)
		                {
		                    writer.write((byte[])args[1], 0, 16);
		                }
		                writer.write((byte[])args[2]);
	
		                writer.close();
		                stream.close();
	
		                return stream.toByteArray();
		            }
	            }
			}
		};

        if (key != null && !key.equals(""))
        {
            switch (_Lock)
            {
                case None:
                    {
                        encryptBytes = setdata.run(_Lock, null, bs);
                        break;
                    }
                case AES:
                    {
                        byte[] IV = new byte[16];
                        new Random(UUID.randomUUID().hashCode()).nextBytes(IV);
                        encryptBytes = setdata.run(_Lock, IV, AESEncrypt(bs, IV, key));
                        break;
                    }
                case RSA:
                    {
                        byte[] IV = new byte[16];
                        new Random(UUID.randomUUID().hashCode()).nextBytes(IV);
                        encryptBytes = setdata.run(_Lock, IV, RSAEncrypt(key, IV, bs));
                        break;
                    }
            }
        }
        else
        {
            encryptBytes = setdata.run(LockType.None, null, bs);
        }
        return Compress(encryptBytes);
    }

    static public byte[] UnLock(byte[] bs, String key) throws Exception
    {
        final int TypeLen = 1;
        final int IVLen = 16;

        byte[] _out = null;
        try(ByteArrayInputStream stream = new ByteArrayInputStream(bs))
        {
        	try(DataInputStream reader = new DataInputStream(stream))
        	{
                LockType _Lock = LockType.values()[reader.readByte()];
                switch (_Lock)
                {
                    case None:
                        {
                        	_out = new byte[bs.length - TypeLen];
                            reader.read(_out);
                            break;
                        }
                    case AES:
                        {
                            byte[] IV = new byte[IVLen];
                            reader.read(IV);
                            byte[] data = new byte[bs.length - TypeLen - IVLen];
                            reader.read(data);
                            _out = AESDecrypt(data, IV, key);
                            break;
                        }
                    case RSA:
                        {
                            byte[] IV = new byte[IVLen];
                            reader.read(IV);
                            byte[] data = new byte[bs.length - TypeLen - IVLen];
                            reader.read(data);
                            _out = RSADecrypt(key, IV, data);
                            break;
                        }
                }
                reader.close();
                stream.close();
                return _out;
        	}
        }
    }

    static public byte[] SignData(String privateKey, String AESKey, byte[] content) throws Exception
    {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
	        try (DataOutputStream writer = new DataOutputStream(stream))
	        {
		        {
		            byte[] data = Lock(content, AESKey, LockType.AES);
		            writer.write(data.length);
		            writer.write(data);
		            writer.write(RSASignData(privateKey, content, Signature.getInstance("SHA256withRSA")));
		            writer.close();
		            stream.close();
		            return stream.toByteArray();
		        }
	        }
        }
    }

    static public boolean VerifyData(String publicKey, String AESKey, byte[] signature) throws Exception
    {
    	try (ByteArrayInputStream stream = new ByteArrayInputStream(signature))
    	{
	    	try (DataInputStream reader = new DataInputStream(stream))
	        {
	            int datalen = reader.readInt();
	            byte[] data = new byte[datalen];
	            reader.read(data);
	            data = UnLock(data, AESKey);
	            byte[] sign = new byte[signature.length - datalen];
	            reader.read(sign);
	            return RSAVerifyData(publicKey, data, sign, Signature.getInstance("SHA256withRSA"));
	        }
    	}
    }

    static public byte[] Compress(byte[] bytes) throws Exception
    {
        int byteLength = bytes.length;
        byte[] bytes2;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
        	try (GZIPOutputStream compressionStream = new GZIPOutputStream(stream))
            {
                compressionStream.write(bytes);
            }
            stream.close();

            bytes2 = stream.toByteArray();
        }

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
	        try (DataOutputStream writer = new DataOutputStream(stream))
	        {
	            writer.writeBoolean(byteLength > bytes2.length);
	
	            writer.write(byteLength);
	            if (byteLength > bytes2.length)
	            {
	                writer.write(bytes2.length);
	                writer.write(bytes2);
	            }
	            else
	            {
	                writer.write(bytes);
	            }
	            writer.close();
	            stream.close();
	            return stream.toByteArray();
	        }
        }
    }

    static public byte[] Decompress(byte[] _bytes, int index, int[] length) throws Exception
    {
        boolean compress;
        int q;
        byte[] bs;

        try (ByteArrayInputStream stream = new ByteArrayInputStream(_bytes))
        {
	        try (DataInputStream reader = new DataInputStream(stream))
	        {
	            stream.skip(index);
	            compress = reader.readBoolean();
	            q = reader.readInt();
	            bs = new byte[compress ? reader.readInt() : q];
	            reader.read(bs);
	
	            reader.close();
	            stream.close();
	        }
        }
        
        byte[] str;
        if (compress)
        {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(bs))
            {
                str = new byte[q];

                try (GZIPInputStream  decompressionStream = new GZIPInputStream(stream))
                {
                    decompressionStream.read(str, 0, q);
                }
            }
            length[0] = bs.length + 9;
        }
        else
        {
            str = bs;
            length[0] = bs.length + 5;
        }
        return str;
    }
}

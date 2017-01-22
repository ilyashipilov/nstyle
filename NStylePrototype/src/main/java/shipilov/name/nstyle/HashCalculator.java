package shipilov.name.nstyle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.MessageDigest;

/**
 * Расчет хэша для файла.
 * Сервер обработки при проверке должен использовать тот же алгоритм расчета и выполнять обработку только при их совпадении.
 *
 * Для предотвращения риверс-инжиниринга алгоритма класс реализующий расчет должен быть обфуцирован.
 *
 * Created by HOME on 21.01.2017.
 */
public interface HashCalculator {
    String calculate(InputStream file) throws Exception;

    /**
     * В этой реализация расчета мы берем md5 от компонентов: файл + секретный код
     */
    class FirstBytesAndSaltImpl implements HashCalculator {

        public static final String SALT = "secretKey";

        @Override
        public String calculate(InputStream file) throws Exception {
            java.io.SequenceInputStream fileWithSalt = new SequenceInputStream(file, new ByteArrayInputStream(SALT.getBytes()));
            return md5(fileWithSalt);
        }

        private static char[] hexDigits = "0123456789abcdef".toCharArray();

        public static String md5(InputStream is) throws IOException {
            String md5 = "";

            try {
                byte[] bytes = new byte[4096];
                int read = 0;
                MessageDigest digest = MessageDigest.getInstance("MD5");

                while ((read = is.read(bytes)) != -1) {
                    digest.update(bytes, 0, read);
                }

                byte[] messageDigest = digest.digest();

                StringBuilder sb = new StringBuilder(32);

                for (byte b : messageDigest) {
                    sb.append(hexDigits[(b >> 4) & 0x0f]);
                    sb.append(hexDigits[b & 0x0f]);
                }

                md5 = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return md5;
        }

    }
}

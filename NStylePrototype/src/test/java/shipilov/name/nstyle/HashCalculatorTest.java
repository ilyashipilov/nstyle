package shipilov.name.nstyle;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class HashCalculatorTest {

    @Ignore
    @Test
    public void calculate() throws Exception {
        File file = new File("C:\\work\\NStyle_research\\out_friends2.png");
        HashCalculator hashCalculator = new HashCalculator.FirstBytesAndSaltImpl();

        System.out.print(hashCalculator.calculate(new FileInputStream(file)));
    }
}
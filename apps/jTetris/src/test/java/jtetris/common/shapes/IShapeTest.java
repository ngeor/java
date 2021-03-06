package jtetris.common.shapes;

import jtetris.common.BlockType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link IShape}.
 * Created by ngeor on 16/6/2017.
 */
@SuppressWarnings("MagicNumber")
public class IShapeTest {
    private IShape shape;

    @Before
    public void before() {
        shape = new IShape();
    }

    @Test
    public void blockAt() throws Exception {
        for (int row = 0; row < shape.getRows(); row++) {
            for (int col = 0; col < shape.getColumns(); col++) {
                assertEquals(BlockType.I, shape.blockAt(row, col));
            }
        }
    }

    @Test
    public void getColumns() throws Exception {
        assertEquals(1, shape.getColumns());
    }

    @Test
    public void getRows() throws Exception {
        assertEquals(4, shape.getRows());
    }
}

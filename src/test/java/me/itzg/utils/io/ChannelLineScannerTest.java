package me.itzg.utils.io;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class ChannelLineScannerTest {
    private ChannelLineScanner lineScanner;

    @Before
    public void setUp() throws Exception {
        lineScanner = new ChannelLineScanner();
        lineScanner.initDefaultBufferPool();
    }

    @Test
    public void testScanInOne_NoEofNl() throws Exception {
        lineScanner.setBufferSize(5000);

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-no-eof-nl.txt");
        verifyTypical(lineScanner, contentPath);

    }

    private Path loadResourcePath(String resourceName) throws URISyntaxException, FileNotFoundException {
        URL resource = ChannelLineScannerTest.class.getClassLoader().getResource(resourceName);
        if (resource != null) {
            return Paths.get(resource.toURI());
        }
        else {
            throw new FileNotFoundException(resourceName);
        }
    }

    @Test
    public void testScanInOne_WithEofNl() throws Exception {
        lineScanner.setBufferSize(5000);

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-with-eof-nl.txt");
        verifyTypical(lineScanner, contentPath);
    }

    @Test
    public void testDiscontinuePartWay() throws Exception {
        lineScanner.setBufferSize(5000);

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-with-eof-nl.txt");
        try (FileChannel fileChannel = FileChannel.open(contentPath, StandardOpenOption.READ)) {
            ChannelLineScanner.Observer observer = mock(ChannelLineScanner.Observer.class);
            when(observer.observeLine(anyObject(), anyLong()))
                    .thenReturn(false);

            lineScanner.scan(fileChannel, observer);

            verify(observer).observeLine(matchingCharSequence("AAA"), Matchers.eq(0l));
            verifyNoMoreInteractions(observer);
        }
    }

    @Test
    public void testScanInBits() throws Exception {
        lineScanner.setBufferSize(15); // buffer lands in middle of file content

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-with-eof-nl.txt");
        verifyTypical(lineScanner, contentPath);
    }

    private CharSequence matchingCharSequence(String expected) {
        return argThat(new CharSequenceMatchesString(expected));
    }

    private void verifyTypical(ChannelLineScanner lineScanner, Path contentPath) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(contentPath, StandardOpenOption.READ)) {
            ChannelLineScanner.Observer observer = mock(ChannelLineScanner.Observer.class);
            when(observer.observeLine(anyObject(), anyLong())).thenReturn(true);

            lineScanner.scan(fileChannel, observer);

            verify(observer).observeLine(matchingCharSequence("AAA"), Matchers.eq(0l));
            verify(observer).observeLine(matchingCharSequence("BBBB"), Matchers.eq(4l));
            verify(observer).observeLine(matchingCharSequence("CCCCC"), Matchers.eq(9l));
            verify(observer).observeLine(matchingCharSequence("DDDDDD"), Matchers.eq(15l));
            verify(observer).observeEndOfFile(anyLong());
            verifyNoMoreInteractions(observer);
        }
    }

    @Test
    public void testOneLine() throws Exception {
        lineScanner.setBufferSize(5000);

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-oneline-no-eof-nl.txt");

        try (FileChannel fileChannel = FileChannel.open(contentPath, StandardOpenOption.READ)) {
            ChannelLineScanner.Observer observer = mock(ChannelLineScanner.Observer.class);
            when(observer.observeLine(anyObject(), anyLong())).thenReturn(true);

            lineScanner.scan(fileChannel, observer);

            verify(observer).observeLine(matchingCharSequence("DDDDDD"), Matchers.eq(0l));
            verify(observer).observeEndOfFile(6);

            verifyNoMoreInteractions(observer);
        }

    }

    @Test
    public void testEmptyFile() throws Exception {
        lineScanner.setBufferSize(5000);

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-empty-file.txt");

        try (FileChannel fileChannel = FileChannel.open(contentPath, StandardOpenOption.READ)) {
            ChannelLineScanner.Observer observer = mock(ChannelLineScanner.Observer.class);

            lineScanner.scan(fileChannel, observer);
            verify(observer).observeEndOfFile(0);

            verifyNoMoreInteractions(observer);
        }


    }

    @Test
    public void testMultiCharDelimiter() throws Exception {
        lineScanner.setBufferSize(5000);
        lineScanner.setDelimiter("+="); // Windows style

        Path contentPath = loadResourcePath("ChannelLineScannerTest/test-crnl.txt");
        try (FileChannel fileChannel = FileChannel.open(contentPath, StandardOpenOption.READ)) {
            ChannelLineScanner.Observer observer = mock(ChannelLineScanner.Observer.class);
            when(observer.observeLine(anyObject(), anyLong())).thenReturn(true);

            lineScanner.scan(fileChannel, observer);

            verify(observer).observeLine(matchingCharSequence("AAA"), Matchers.eq(0l));
            verify(observer).observeLine(matchingCharSequence("BBBB"), Matchers.eq(5l));
            verify(observer).observeLine(matchingCharSequence("CCCCC"), Matchers.eq(11l));
            verify(observer).observeLine(matchingCharSequence("DDDDDD"), Matchers.eq(18l));
            verify(observer).observeEndOfFile(24);
            verifyNoMoreInteractions(observer);
        }
    }
}
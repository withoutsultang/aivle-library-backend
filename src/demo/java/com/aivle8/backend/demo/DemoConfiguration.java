package com.aivle8.backend.demo;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import com.aivle8.backend.service.ImageGenerationService;
import com.aivle8.backend.service.S3Service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Configuration
@Profile("demo")
public class DemoConfiguration {
    public static final String COVER_PREFIX = "/api/v1/auth/demo/covers/";

    @Bean
    public Map<String, byte[]> demoObjects() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    @Primary
    public AmazonS3 demoAmazonS3(Map<String, byte[]> demoObjects) {
        AmazonS3 client = mock(AmazonS3.class);
        when(client.putObject(any(PutObjectRequest.class))).thenAnswer(call -> {
            PutObjectRequest request = call.getArgument(0);
            demoObjects.put(request.getKey(), request.getInputStream().readAllBytes());
            return null;
        });
        doAnswer(call -> { demoObjects.remove((String) call.getArgument(1)); return null; })
                .when(client).deleteObject(any(String.class), any(String.class));
        return client;
    }

    @Bean
    @Primary
    public S3Service demoStorage(AmazonS3 demoAmazonS3) {
        return new S3Service(demoAmazonS3) {
            @Override public String getPresignedUrl(String key) {
                return key == null ? null : COVER_PREFIX + key;
            }
            @Override public boolean isSameImage(String key, String input) {
                return key != null && (COVER_PREFIX + key).equals(input) || super.isSameImage(key, input);
            }
        };
    }

    @Bean
    @Primary
    public DynamoDbClient demoCounter() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(UpdateItemResponse.builder().build());
        return client;
    }

    @Bean
    @Primary
    public ImageGenerationService demoIllustrations() {
        AtomicInteger sequence = new AtomicInteger();
        return new ImageGenerationService(RestClient.builder().build()) {
            @Override public String generate(String prompt, String size) {
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(cover(sequence.getAndIncrement()));
            }
        };
    }

    @Bean
    public CommandLineRunner demoLibrary(UserRepository users, BookRepository books, Map<String, byte[]> demoObjects) {
        return args -> {
            User author = users.save(User.builder().nickname("서재 작가").email("author@example.test").naverId("demo-author").build());
            User reader = users.save(User.builder().nickname("서재 독자").email("reader@example.test").naverId("demo-reader").build());
            String[] titles = {"도시의 작은 숲", "느리게 걷는 오후", "처음 만나는 클라우드", "여행의 온도", "한 줄의 기록"};
            String[] categories = {"에세이", "소설", "IT", "여행", "에세이"};
            String[] descriptions = {"바쁜 도시 속에서도 작은 초록을 발견하는 사람들의 이야기입니다.", "익숙한 골목을 천천히 걸으며 만난 다섯 번의 오후를 담았습니다.", "로컬에서 만든 서비스를 클라우드로 옮기는 여정을 쉽게 풀어냈습니다.", "낯선 도시에서 발견한 다정한 순간과 오래 남는 풍경을 기록합니다.", "평범한 하루의 한 문장이 나만의 이야기가 되는 과정을 담았습니다."};
            demoObjects.put("ai-images/default-cover.png", cover(0));
            for (int i = 0; i < titles.length; i++) {
                String key = "ai-images/sample-" + i + ".png";
                demoObjects.put(key, cover(i));
                Book book = Book.builder().title(titles[i]).category(categories[i]).coverImageUrl(key)
                        .user(i == 3 ? reader : author).build();
                BookDetail detail = BookDetail.builder().book(book).description(descriptions[i])
                        .content((descriptions[i] + "\n\n우리는 각자의 속도로 이야기를 써 내려갑니다. 작은 발견을 모아 한 권의 책으로 남깁니다.\n\n").repeat(18)).build();
                book.setBookDetail(detail);
                books.save(book);
            }
        };
    }

    private static byte[] cover(int index) {
        String[] backgrounds = {"#224B40", "#A86142", "#274B70", "#8C6880", "#70734D"};
        String[] headings = {"CITY FOREST", "SLOW AFTERNOON", "INTO THE CLOUD", "WARM JOURNEY", "DAILY NOTES"};
        int variant = Math.floorMod(index, backgrounds.length);
        BufferedImage image = new BufferedImage(480, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.decode(backgrounds[variant])); g.fillRect(0, 0, 480, 680);
        g.setColor(new Color(255, 255, 255, 20));
        for (int i = 0; i < 5; i++) g.fillOval(60 + i * 26, 190 + i * 16, 230, 230);
        g.setColor(Color.decode("#F5E9C9"));
        g.fillRoundRect(110, 235, 125, 170, 8, 8); g.fillRoundRect(245, 235, 125, 170, 8, 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        g.drawString("MODU LIBRARY", 38, 72);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        g.drawString(headings[variant], 38, 525);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g.drawString("PORTFOLIO DEMO / SAMPLE COVER", 38, 612);
        g.dispose();
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bytes);
            return bytes.toByteArray();
        } catch (Exception exception) { throw new IllegalStateException("Demo cover creation failed", exception); }
    }
}

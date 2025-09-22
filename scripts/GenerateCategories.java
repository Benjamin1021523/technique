import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自動生成 GitHub Pages 分類目錄的 Java 腳本
 * 掃描 docs/ 目錄下的所有 .md 文件，生成分類列表
 */
public class GenerateCategories {
    
    private static final String DOCS_DIR = "docs";
    private static final List<String> TOPIC_LIST;

    static {
        try {
            TOPIC_LIST = Stream.of(FileUtil.readContent(Path.of(DOCS_DIR, ".order")).split("\n")).map(String::trim).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String INDEX_FILE = "docs/index.md";
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("<!--關鍵字:\\s*(.+?)-->", Pattern.DOTALL);
    private static final Pattern TITLE_PATTERN = Pattern.compile("^title:\\s*(.+)$", Pattern.MULTILINE);
    
    public static void main(String[] args) {
        // 設置控制台編碼
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (Exception e) {
            // 忽略編碼設置錯誤
        }
        
        System.out.println("開始掃描文件...");
        
        try {
            Map<String, List<Document>> categories = scanDocuments();
            
            if (categories.isEmpty()) {
                System.out.println("No files found");
                return;
            }
            
            System.out.println("Found " + categories.size() + " categories:");
            for (Map.Entry<String, List<Document>> entry : categories.entrySet()) {
                String category = entry.getKey();
                List<Document> docs = entry.getValue();
                System.out.println("  - " + category + ": " + docs.size() + " files");
                for (Document doc : docs) {
                    updateExtension(doc);
                    System.out.println("    * " + doc.title + " (keywords: " + doc.keywords + ")");
                }
            }
            
            System.out.println("\nUpdating index.md...");
            updateIndexFile(categories);
            
            System.out.println("Done!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 掃描 docs/ 目錄下的所有文件
     */
    private static Map<String, List<Document>> scanDocuments() throws IOException {
        Map<String, List<Document>> categories = new TreeMap<>();
        Path docsPath = Paths.get(DOCS_DIR);
        
        if (!Files.exists(docsPath)) {
            System.out.println("docs/ directory does not exist");
            return categories;
        }
        
        // 依照目錄下.order紀錄的目錄名取得個別的index.md檔，從中取得檔案資訊
        for (String topic : TOPIC_LIST) {
            List<String> rootOrder = Stream.of(FileUtil.readContent(Paths.get(DOCS_DIR, topic, ".order")).split("\n")).map(String::trim).toList();
            for (String folderName : rootOrder) {
                processMarkdownFile(Paths.get(DOCS_DIR, topic, folderName, "index.md"), categories);
            }
        }
        
        return categories;
    }

    private static void updateExtension(Document doc) throws IOException {
        Path index = Path.of(DOCS_DIR, doc.path);
        Path parent = index.getParent();
        Path order = Path.of(parent.toString(), ".order");

        if (!Files.exists(order)) {
            System.out.println(order.toString());
            return;
        }

        List<String> subpageList = Stream.of(FileUtil.readContent(order).split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        String originContent = FileUtil.readContent(index);
        if (!originContent.contains("<!--Extension Start-->") || !originContent.contains("<!--Extension End-->")) {
            return;
        }

        String newExtension = "<!--Extension Start-->\n";
        for (String subpage : subpageList) {
            newExtension += "1. [" + subpage + "](./" + subpage + ")\n";
        }
        newExtension += "<!--Extension End-->";

        originContent = originContent.substring(0, originContent.indexOf("<!--Extension Start-->"))
                + newExtension
                + originContent.substring(originContent.indexOf("<!--Extension End-->") + "<!--Extension End-->".length());
        Files.writeString(index, originContent, StandardCharsets.UTF_8);
    }
    
    /**
     * 處理單個 Markdown 文件
     */
    private static void processMarkdownFile(Path mdFile, Map<String, List<Document>> categories) throws IOException {
        Path relativePath = Paths.get(DOCS_DIR).relativize(mdFile);
        List<String> pathParts = new ArrayList<>();
        
        // 手動分割路徑，避免正則表達式問題
        Path current = relativePath;
        while (current != null && !current.toString().isEmpty()) {
            pathParts.add(0, current.getFileName().toString());
            current = current.getParent();
        }
        
        if (pathParts.size() > 1) {
            String category = pathParts.get(0); // 第一個目錄作為分類
            String filename = pathParts.get(pathParts.size() - 1).replace(".md", ""); // 文件名（去掉 .md）
            
            // 讀取文件內容
            String content = Files.readString(mdFile, java.nio.charset.StandardCharsets.UTF_8);
            
            // 提取標題
            String title = extractTitle(content, filename);
            
            // 提取關鍵字
            List<String> keywords = extractKeywords(content);
            
            categories.computeIfAbsent(category, k -> new ArrayList<>())
                .add(new Document(title, filename, relativePath.toString().replace("\\", "/"), keywords, content.contains("<!--Finish-->")));
        }
    }
    
    /**
     * 從文件內容中提取標題
     */
    private static String extractTitle(String content, String filename) {
        Matcher matcher = TITLE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return filename;
    }
    
    /**
     * 從文件內容中提取關鍵字
     */
    private static List<String> extractKeywords(String content) {
        Matcher matcher = KEYWORD_PATTERN.matcher(content);
        if (matcher.find()) {
            String keywordsStr = matcher.group(1);
            return Arrays.stream(keywordsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    /**
     * 生成分類的 Markdown 內容
     */
    private static String generateCategoryMarkdown(Map<String, List<Document>> categories) {
        if (categories.isEmpty()) {
            return "## 技術文件目錄\n\n暫無文件。\n";
        }
        
        StringBuilder markdown = new StringBuilder();
        markdown.append("## 技術文件目錄\n\n");
        
        for (Map.Entry<String, List<Document>> entry : categories.entrySet()) {
            String category = entry.getKey();
            List<Document> docs = entry.getValue();
            
            markdown.append("<details>\n");
            markdown.append("<summary><strong>[").append(category).append("] 分類</strong></summary>\n\n");

            if (!docs.isEmpty()) {
                markdown.append("<ul>\n");

                final String liTemplate = """
                          <li>
                            <a href="$Path">[$Category] $Title</a>$NotFinish
                            <ul>
                              <li>關鍵字: $Keyword</li>
                            </ul>
                          </li>
                        """;

                for (Document doc : docs) {
                    String liContent = liTemplate
                            .replace("$Path", doc.path.replace("index.md", ""))
                            .replace("$Category", category)
                            .replace("$Title", doc.title)
                            .replace("$NotFinish", doc.isFinish ? "" : "(未完成)")
                            .replace("$Keyword", String.join(", ", doc.keywords.stream().map(s -> String.format("<code>%s</code>", s)).toList()));
                    markdown.append(liContent);
                }
                markdown.append("</ul>\n");
            }
            
            markdown.append("\n</details>\n\n");
        }
        return markdown.toString();
    }
    
    /**
     * 更新 index.md 文件
     */
    private static void updateIndexFile(Map<String, List<Document>> categories) throws IOException {
        Path indexPath = Paths.get(INDEX_FILE);
        
        if (!Files.exists(indexPath)) {
            System.out.println("docs/index.md does not exist");
            return;
        }
        
        // 讀取現有內容
        String content = Files.readString(indexPath, java.nio.charset.StandardCharsets.UTF_8);
        
        // 生成新的分類內容
        String newCategories = generateCategoryMarkdown(categories);
        System.out.println(newCategories);
        
        // 替換分類部分
        int replaceFrom = content.indexOf("<!--生成區域Start-->") + "<!--生成區域Start-->".length();
        int replaceTo = content.indexOf("<!--生成區域End-->");

        content = content.substring(0, replaceFrom) + "\n" + newCategories + "\n" + content.substring(replaceTo);
        
        // 寫回文件
        Files.writeString(indexPath, content, StandardCharsets.UTF_8);
        
        System.out.println("Updated " + indexPath);
    }

    /**
     * 文件信息類
     */
    private record Document(String title, String filename, String path, List<String> keywords, boolean isFinish) {}
}

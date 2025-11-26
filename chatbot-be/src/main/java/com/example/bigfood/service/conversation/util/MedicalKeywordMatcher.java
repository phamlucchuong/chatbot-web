package com.example.bigfood.service.conversation.util;

import com.example.bigfood.repository.DiseaseRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalKeywordMatcher {
    
    private final TextNormalizer textNormalizer;
    private final DiseaseRepository diseaseRepository;

    public boolean isDeclaringDisease(String message) {
        String normalized = textNormalizer.normalize(message);
        
        String[] keywords = {
            "bị", "đang bị", "đang mắc", "mắc", "chẩn đoán", 
            "được chẩn đoán", "tôi bị", "tôi mắc", "bị chẩn đoán",
            "mắc phải", "mắc bệnh"
        };
        
        boolean hasKeyword = false;
        for (String kw : keywords) {
            if (normalized.contains(kw)) {
                hasKeyword = true;
                break;
            }
        }
        
        if (!hasKeyword) return false;
        
        return diseaseRepository.findAll().stream()
            .anyMatch(disease -> {
                String diseaseName = textNormalizer.normalize(disease. getName());
                return !diseaseName.isBlank() && normalized.contains(diseaseName);
            });
    }
    
    public int calculateMedicalScore(String message) {
        String normalized = textNormalizer.normalize(message);
        
        String[] medicalKeywords = {
            "đau", "sốt", "ho", "nôn", "buồn nôn", "chóng mặt", "mệt",
            "tiêu chảy", "táo bón", "khó thở", "ngứa", "phát ban", "sưng",
            "viêm", "chảy máu", "ói mửa", "đau đầu", "đau bụng", "sổ mũi",
            "nhức", "co giật", "run rẩy", "ớn lạnh", "mệt mỏi", "yếu",
            "bệnh", "triệu chứng", "điều trị", "thuốc", "khám", "bác sĩ"
        };
        
        int score = 0;
        for (String keyword : medicalKeywords) {
            if (normalized.contains(keyword)) {
                score++;
            }
        }
        return score;
    }
    
    public int calculateOffTopicScore(String message) {
        String normalized = textNormalizer.normalize(message);
        
        Map<String, Integer> offTopicCategories = new HashMap<>();
        
        offTopicCategories.put("thời tiết", 3);
        offTopicCategories.put("dự báo thời tiết", 3);
        offTopicCategories.put("nhiệt độ hôm nay", 3);
        offTopicCategories.put("trời mưa", 3);
        offTopicCategories.put("nấu ăn", 3);
        offTopicCategories.put("công thức món", 3);
        offTopicCategories.put("làm bánh", 3);
        offTopicCategories.put("lập trình", 3);
        offTopicCategories.put("code", 3);
        offTopicCategories.put("python", 3);
        offTopicCategories.put("java", 3);
        offTopicCategories.put("phim", 3);
        offTopicCategories.put("xem phim", 3);
        offTopicCategories.put("game", 3);
        offTopicCategories.put("chơi game", 3);
        offTopicCategories.put("bóng đá", 3);
        offTopicCategories.put("world cup", 3);
        offTopicCategories.put("thể thao", 3);
        offTopicCategories.put("nhạc", 3);
        offTopicCategories.put("nghe nhạc", 3);
        offTopicCategories.put("du lịch", 3);
        offTopicCategories.put("máy bay", 3);
        offTopicCategories.put("xe hơi", 3);
        offTopicCategories.put("đất nhà", 3);
        offTopicCategories.put("tiền tệ", 3);
        offTopicCategories.put("ngân hàng", 3);
        offTopicCategories. put("siêu thị", 3);
        offTopicCategories.put("mua sắm", 3);
        offTopicCategories.put("tình yêu", 3);
        offTopicCategories.put("tình cảm", 3);
        offTopicCategories.put("hôn nhân", 3);
        offTopicCategories.put("công việc", 3);
        offTopicCategories.put("học tập", 3);
        
        offTopicCategories.put("youtube", 2);
        offTopicCategories.put("tiktok", 2);
        offTopicCategories.put("facebook", 2);
        offTopicCategories.put("instagram", 2);
        offTopicCategories.put("twitter", 2);
        offTopicCategories.put("zalo", 2);
        offTopicCategories.put("gmail", 2);
        offTopicCategories.put("email", 2);
        offTopicCategories.put("điện thoại", 2);
        offTopicCategories. put("máy tính", 2);
        
        int score = 0;
        for (Map.Entry<String, Integer> entry : offTopicCategories.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }
        
        return score;
    }
    
    public boolean hasSpecificSymptoms(String message) {
        String normalized = textNormalizer. normalize(message);
        
        String[] symptomKeywords = {
            "sốt", "đau đầu", "đau", "ho", "nôn", "buồn nôn", "tiêu chảy",
            "chóng mặt", "mệt mỏi", "khó thở", "ngứa", "phát ban",
            "sưng", "chảy máu", "viêm", "khó ngủ", "run rẩy", "ớn lạnh",
            "sổ mũi", "nhức", "mệt", "yếu", "uể oải", "ốm"
        };
        
        int count = 0;
        for (String keyword : symptomKeywords) {
            if (normalized.contains(keyword)) {
                count++;
            }
        }
        
        return count >= 1;
    }
    
    public boolean isVagueComplaint(String message) {
        String normalized = textNormalizer. normalize(message);
        
        String[] vaguePatterns = {
            "mệt", "không khỏe", "bị bệnh", "có vấn đề", "không ổn",
            "khó chịu", "sao sao", "lạ lạ", "không được khỏe",
            "cảm thấy không tốt", "hơi lạ", "hơi không ổn"
        };
        
        for (String pattern : vaguePatterns) {
            if (normalized.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isAskingDiseaseInfo(String message) {
        String normalized = textNormalizer.normalize(message);
        
        String[] infoKeywords = {
            "nguyên nhân", "tại sao", "vì sao", "do đâu", "gây ra", "bị bởi",
            "triệu chứng", "dấu hiệu", "biểu hiện", "có dấu hiệu gì",
            "có triệu chứng gì", "những triệu chứng", "các triệu chứng",
            "là gì", "mô tả", "thế nào", "giới thiệu", "bệnh này",
            "định nghĩa", "khái niệm", "chi tiết",
            "có thể lây nhiễm", "lây nhiễm", "phát triển", "biến chứng",
            "tiến hành", "tiến triển", "tái phát", "tái xuất",
            "tiên lượng", "dự đoán", "mức độ", "nhóm nguy cơ", "nhóm bệnh",
            "tiêu chuẩn chẩn đoán", "xét nghiệm", "chẩn đoán",
            "hậu quả", "ảnh hưởng", "khác", "tương đương", "tương tự"
        };
        
        for (String keyword : infoKeywords) {
            if (normalized. contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ⭐ QUAN TRỌNG: Kiểm tra câu hỏi liên quan đến ngữ cảnh y tế
     */
    public boolean isAskingMedicalContext(String message) {
        String normalized = textNormalizer.normalize(message);
        
        String[] contextKeywords = {
            // Đi khám, cấp cứu
            "có nên đi khám", "có cần đi khám", "nên đi khám", "phải đi khám",
            "có nên đi bệnh viện", "có cần đi bệnh viện", "nên đi bệnh viện",
            "có phải đi cấp cứu", "có cần cấp cứu", "nên gọi cấp cứu", 
            "cấp cứu ngay", "đi cấp cứu", "cấp cứu", "gọi cấp cứu", "khẩn cấp",
            "đi khám gấp", "khám gấp", "gấp", "có cần gặp bác sĩ", "nên gặp bác sĩ",
            "tới bệnh viện", "vào viện", "nhập viện", "có nguy hiểm không",
            "có nguy hiểm", "nguy hiểm", "nguy kịch", "tính cấp cứu",
            
            // Sau khi ra viện, phẫu thuật
            "sau khi ra viện", "sau ra viện", "sau điều trị",
            "sau phẫu thuật", "sau khi phẫu thuật", "sau khi chữa",
            "sau chữa", "sau bệnh", "hồi phục", "hồi phục nhanh",
            "quá trình hồi phục", "phục hồi", "tái phát", "tái xuất",
            
            // Chế độ ăn uống
            "nên ăn gì", "có nên ăn", "ăn cái gì", "kiêng ăn cái gì",
            "kiêng ăn", "nên ăn", "có nên ăn", "nên uống gì",
            "có nên uống", "nên dừng uống", "dừng ăn", "tránh ăn",
            "thực phẩm nào", "đồ uống nào", "thực phẩm tốt", "đồ uống tốt",
            "ăn nhất", "uống nhất", "nên kiêng", "có kiêng", "kiêng cừu",
            "chế độ ăn", "dinh dưỡng", "dưỡng chất", "bổ sung", "ăn gì",
            "ăn uống", "chế độ", "hội chứng", "nên an", "kiêng",
            
            // Thuốc
            "thuốc kháng sinh", "thuốc giảm đau", "thuốc hạ sốt", "thuốc nào",
            "uống thuốc", "ngừng thuốc", "dừng thuốc", "nên uống thuốc gì",
            "có uống thuốc", "có nên uống thuốc", "dùng thuốc", 
            "liều lượng", "cách uống", "tương tác thuốc", "tác dụng phụ", 
            "dị ứng", "kiêng rượu", "kiêng bia", "không uống rượu", "không uống bia",
            
            // Hoạt động, sinh hoạt
            "có nên tập", "nên tập thể dục", "có được tập thể dục", "tập thể dục",
            "có được vận động", "nên vận động", "được phép vận động",
            "có nên nghỉ ngơi", "nên nghỉ ngơi", "cần nghỉ ngơi",
            "có được đi làm", "nên đi làm", "có được lao động",
            "có được đi ra ngoài", "nên ở nhà", "gắm ấm", "nên gắm ấm",
            "có được gắm ấm", "tránh gió", "tránh lạnh", "gió lạnh",
            "có được tắm", "tắm nước", "gắm kín", "mặc ấm",
            "nên nằm", "nên ngồi", "tư thế", "cách nằm",
            
            // Thăm khám, theo dõi
            "có cần tái khám", "cần tái khám", "tái khám", "khám lại",
            "có cần xét nghiệm", "xét nghiệm lại", "kiểm tra", "theo dõi",
            "có được tiêm chủng", "tiêm vaccine", "chủng ngừa",
            
            // Thời gian hồi phục
            "bao lâu khỏi", "bao nhiêu ngày", "bao nhiêu tháng", "bao lâu",
            "mất bao lâu", "hồi phục mau", "hồi phục lâu", "cần bao lâu",
            
            // Xử trí, làm gì
            "xử trí", "tự xử trí", "xử lý", "làm gì", "nên làm gì",
            "phải làm gì", "cần làm gì", "có nên làm", "nên hay không",
            "có được làm", "được phép làm",
            "nên tránh", "có nên tránh", "tránh gì", "kiêng gì",
            
            // Lời khuyên thực dụng khác
            "có nên", "có được", "được phép", "có phải", "cần không",
            "như thế nào", "phải sao", "sao vậy", "mách nước",
            "chăm sóc", "cách chăm sóc", "lâm sàng", "khám",
            "trẻ nhỏ", "bé", "cháu", "hạ sốt", "giảm sốt",
            "sơ cứu", "cứu", "sơ cứu đầu tiên"
        };
        
        int contextScore = 0;
        for (String keyword : contextKeywords) {
            if (normalized.contains(keyword)) {
                contextScore++;
            }
        }
        
        if (contextScore > 0) {
            // Loại trừ nếu là hỏi thông tin bệnh thuần túy (không có hành động)
            if (isAskingDiseaseInfo(message) && !hasActionKeyword(message)) {
                return false;
            }
            
            // Loại trừ nếu là triệu chứng thuần túy (không có hành động)
            if (hasSpecificSymptoms(message) && !hasActionKeyword(message)) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean hasActionKeyword(String message) {
        String normalized = textNormalizer.normalize(message);
        String[] actionKeywords = {
            "có nên", "có cần", "nên", "phải", "có được", "được phép",
            "có phải", "cần không", "sao vậy", "như thế nào",
            "xử trí", "làm gì", "kiêng gì", "tránh", "ngừng", "dừng"
        };
        
        for (String keyword : actionKeywords) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOffTopic(String message) {
        int score = calculateOffTopicScore(message);
        return score >= 2;
    }
}
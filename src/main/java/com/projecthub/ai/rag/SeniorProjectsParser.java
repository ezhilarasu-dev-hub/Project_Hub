package com.projecthub.ai.rag;

import com.projecthub.entity.KnowledgeDocument;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class SeniorProjectsParser {

    public List<KnowledgeDocument> parse(String content) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        if (!StringUtils.hasText(content)) {
            return documents;
        }

        // Split on record separators ("---" or blank lines between PROJECT headers)
        String[] blocks = content.split("(?m)^---+$");

        for (String block : blocks) {
            String trimmedBlock = block.trim();
            if (trimmedBlock.isEmpty()) {
                continue;
            }

            KnowledgeDocument doc = parseSingleRecord(trimmedBlock);
            if (doc != null && StringUtils.hasText(doc.getTitle())) {
                documents.add(doc);
            }
        }

        return documents;
    }

    private KnowledgeDocument parseSingleRecord(String block) {
        String title = null;
        String batch = null;
        String authors = null;
        StringBuilder description = new StringBuilder();
        String technologies = null;

        String currentField = null;

        try (BufferedReader reader = new BufferedReader(new StringReader(block))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.equalsIgnoreCase("PROJECT")) {
                    continue;
                }

                if (trimmedLine.toLowerCase().startsWith("title:")) {
                    title = trimmedLine.substring(6).trim();
                    currentField = "title";
                } else if (trimmedLine.toLowerCase().startsWith("batch:")) {
                    batch = trimmedLine.substring(6).trim();
                    currentField = "batch";
                } else if (trimmedLine.toLowerCase().startsWith("authors:") || trimmedLine.toLowerCase().startsWith("author:")) {
                    int colonIndex = trimmedLine.indexOf(':');
                    authors = trimmedLine.substring(colonIndex + 1).trim();
                    currentField = "authors";
                } else if (trimmedLine.toLowerCase().startsWith("technologies:") || trimmedLine.toLowerCase().startsWith("technology:")) {
                    int colonIndex = trimmedLine.indexOf(':');
                    technologies = trimmedLine.substring(colonIndex + 1).trim();
                    currentField = "technologies";
                } else if (trimmedLine.toLowerCase().startsWith("description:")) {
                    description.append(trimmedLine.substring(12).trim());
                    currentField = "description";
                } else {
                    // Continuation of previous multi-line field
                    if ("description".equals(currentField)) {
                        if (description.length() > 0) {
                            description.append(" ");
                        }
                        description.append(trimmedLine);
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }

        if (!StringUtils.hasText(title)) {
            return null;
        }

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(title);
        doc.setBatch(batch != null ? batch : "Unknown");
        doc.setAuthors(authors != null ? authors : "Not specified");
        doc.setDescription(description.toString());
        doc.setTechnologies(technologies != null ? technologies : "General");

        // Construct normalized searchable text representation
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(". ");
        if (StringUtils.hasText(doc.getDescription())) {
            sb.append(doc.getDescription()).append(". ");
        }
        if (StringUtils.hasText(doc.getTechnologies())) {
            sb.append("Technologies: ").append(doc.getTechnologies()).append(". ");
        }
        if (StringUtils.hasText(doc.getBatch())) {
            sb.append("Batch: ").append(doc.getBatch()).append(". ");
        }
        doc.setNormalizedContent(sb.toString().trim());

        return doc;
    }
}

package com.example.resumematcher;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@SpringBootTest
class ResumematcherApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void generateSampleResumePdf() throws IOException {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);

			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
				contentStream.newLineAtOffset(50, 750);
				contentStream.showText("Jane Doe - Full Stack Developer");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				contentStream.newLineAtOffset(0, -30);
				contentStream.showText("Email: jane.doe@example.com | Phone: 555-0199 | GitHub: github.com/janedoe");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
				contentStream.newLineAtOffset(0, -40);
				contentStream.showText("Summary");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
				contentStream.newLineAtOffset(0, -20);
				contentStream.showText("Experienced software developer specializing in building scalable web applications with Java,");
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText("Spring Boot, React, and PostgreSQL. Passionate about writing clean, maintainable code.");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
				contentStream.newLineAtOffset(0, -40);
				contentStream.showText("Skills");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
				contentStream.newLineAtOffset(0, -20);
				contentStream.showText("Languages: Java, JavaScript, HTML, CSS, SQL");
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText("Frameworks: Spring Boot, React, Node.js, JUnit");
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText("Tools: Git, Docker, Maven, VS Code");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
				contentStream.newLineAtOffset(0, -40);
				contentStream.showText("Experience");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
				contentStream.newLineAtOffset(0, -20);
				contentStream.showText("Software Engineer - Tech Solutions Inc (2022 - Present)");

				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText("- Developed RESTful APIs using Spring Boot, reducing data latency by 20%.");
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText("- Built responsive frontend components using React and plain CSS.");
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText("- Collaborated with cross-functional teams to design database schemas.");

				contentStream.endText();
			}

			File targetFile = new File("../sample_resume.pdf");
			document.save(targetFile);
			System.out.println("Generated sample resume PDF at: " + targetFile.getAbsolutePath());
		}
	}
}

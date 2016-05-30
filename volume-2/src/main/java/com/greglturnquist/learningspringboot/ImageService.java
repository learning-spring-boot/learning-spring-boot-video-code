/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.learningspringboot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Greg Turnquist
 */
@Service
public class ImageService {

	private static String UPLOAD_ROOT = "upload-dir";

	private final ImageRepository repository;
	private final ApplicationContext ctx;

	@Autowired
	public ImageService(ImageRepository repository, ApplicationContext ctx) {
		this.repository = repository;
		this.ctx = ctx;
	}

	public Page<Image> findPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Resource findOneImage(String filename) {
		return ctx.getResource("file:" + UPLOAD_ROOT + "/" + filename);
	}

	public void createImage(MultipartFile file) throws IOException {
		if (!file.isEmpty()) {
			Files.copy(file.getInputStream(), Paths.get(UPLOAD_ROOT, file.getOriginalFilename()));
			repository.save(new Image(file.getOriginalFilename()));
		}
	}

	public void deleteImage(String filename) throws IOException {
		final Image byName = repository.findByName(filename);
		repository.delete(byName);
		Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
	}

	@Bean
	CommandLineRunner setUp() {
		return (args) -> {
			FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));

			new File(UPLOAD_ROOT).mkdir();

			FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/test"));
			repository.save(new Image("test"));

			FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/test2"));
			repository.save(new Image("test2"));

			FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/test3"));
			repository.save(new Image("test3"));
		};
	}


}

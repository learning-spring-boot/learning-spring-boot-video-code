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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Greg Turnquist
 */
@Controller
public class HomeController {

	private static final Logger log = LoggerFactory.getLogger(HomeController.class);

	private static final String BASE_PATH = "/images";
	private static final String FILENAME = "{filename:.+}";

	private final ImageService imageService;

	@Autowired
	public HomeController(ImageService imageService) {
		this.imageService = imageService;
	}

	@RequestMapping(method = RequestMethod.POST, value = BASE_PATH)
	@ResponseBody
	public ResponseEntity<?> createFile(@RequestParam("file") MultipartFile file,
							 RedirectAttributes redirectAttributes) {
		try {
			imageService.createImage(file);
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.header(HttpHeaders.LOCATION, BASE_PATH + "/" + file.getOriginalFilename() + "/raw")
					.body("Successfully uploaded " + file.getOriginalFilename());
		} catch (IOException e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to upload " + file.getName() + " => " + e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/" + FILENAME)
	@ResponseBody
	public ResponseEntity<?> deleteFile(@PathVariable String filename,
							 RedirectAttributes redirectAttributes) {
		try {
			imageService.deleteImage(filename);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Successfully deleted " + filename);
		} catch (IOException e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to delete " + filename + " => " + e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/" + FILENAME + "/raw")
	@ResponseBody
	public ResponseEntity<?> oneRawImage(@PathVariable String filename) {
		org.springframework.core.io.Resource file = imageService.findOneImage(filename);
		try {
			return ResponseEntity.ok()
					.contentLength(file.contentLength())
					.contentType(MediaType.IMAGE_JPEG)
					.body(new InputStreamResource(file.getInputStream()));
		} catch (IOException e) {
			log.error(e.getMessage());
			return ResponseEntity.badRequest()
					.body("Couldn't find " + filename + " => " + e.getMessage());
		}
	}


}

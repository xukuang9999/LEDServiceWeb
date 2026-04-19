package com.zhglxt.web.controller.cms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Public metadata endpoints for crawlers and AI tools.
 */
@Controller
public class PublicMetadataController
{
    private static final String CMS_STATIC_ROOT = "static/cms/";

    @GetMapping(value = { "/robots.txt", "/cms/robots.txt" }, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> robots() throws IOException
    {
        return serveResource("robots.txt", MediaType.TEXT_PLAIN);
    }

    @GetMapping(value = { "/llms.txt", "/cms/llms.txt" }, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> llms() throws IOException
    {
        return serveResource("llms.txt", MediaType.TEXT_PLAIN);
    }

    @GetMapping(value = { "/llms-full.txt", "/cms/llms-full.txt" }, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> llmsFull() throws IOException
    {
        return serveResource("llms-full.txt", MediaType.TEXT_PLAIN);
    }

    @GetMapping(value = { "/sitemap.xml", "/cms/sitemap.xml" }, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<String> sitemap() throws IOException
    {
        return serveResource("sitemap.xml", MediaType.APPLICATION_XML);
    }

    private ResponseEntity<String> serveResource(String fileName, MediaType mediaType) throws IOException
    {
        ClassPathResource resource = new ClassPathResource(CMS_STATIC_ROOT + fileName);
        if (!resource.exists())
        {
            return ResponseEntity.notFound().build();
        }

        String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(mediaType).body(body);
    }
}

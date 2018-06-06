package hp.hpfb.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ValidationXmlController {
	private static final String VIEW_NAME = "hpfb.validator";
	
    private static final String UPLOADED_FOLDER = "c://temp//files//";
    private Path root = Paths.get(UPLOADED_FOLDER);
	@RequestMapping(value="/validateXML", method=RequestMethod.GET)
    public String validateXml(String name, Model model, HttpServletRequest req) throws Exception {
        return "validateXml";
    }
    @RequestMapping(value="/validateXML", method=RequestMethod.POST)
    public String validation(@RequestParam("file") MultipartFile file, Model model,
            RedirectAttributes redirectAttributes) {
    	if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE);

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("files", loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(ValidationXmlController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
        return "validateXml";
    }
    @RequestMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file;
		try {
			file = loadAsResource(filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
	                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    public Resource loadAsResource(String filename) throws Exception {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new Exception(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new Exception("Could not read file: " + filename, e);
        }
    }
    public Path load(String filename) {
        return root.resolve(filename);
    }

    public Stream<Path> loadAll(){
    	try {
			return Files.walk(root, 1).filter(path -> !path.equals(root)).map(root::relativize);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleStorageFileNotFound(Exception exc) {
        return ResponseEntity.notFound().build();
    }
}

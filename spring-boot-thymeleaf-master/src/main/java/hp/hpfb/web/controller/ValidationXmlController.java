package hp.hpfb.web.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import hp.hpfb.web.service.XmlSchemaValidatingService;

@Controller
public class ValidationXmlController {
	
	@Value("${file.directory}")
    private String UPLOADED_FOLDER;
	
	@Autowired
	private XmlSchemaValidatingService service;
	
	
    private Path root;

	@RequestMapping(value="/validateXML", method=RequestMethod.GET)
    public String validateXml(Model model, HttpServletRequest req) throws Exception {
        String userPath = UPLOADED_FOLDER + req.getSession().getId() + "/";
        model.addAttribute("files", loadAll(Paths.get(userPath)).map(
                path -> MvcUriComponentsBuilder.fromMethodName(ValidationXmlController.class,
                        "serveFile", req.getSession().getId(), path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
		return "validateXml";
    }
    @RequestMapping(value="/validateXML", method=RequestMethod.POST)
    public String validation(@RequestParam("file") MultipartFile file, Model model,
            RedirectAttributes redirectAttributes, HttpServletRequest req) {
    	if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            String filename = UPLOADED_FOLDER + req.getSession().getId() + "//" + file.getOriginalFilename();
            Path path = Paths.get(filename);
            Files.write(path, bytes, StandardOpenOption.CREATE);

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
            
            List<String> errors = service.verifyXml(filename);
            model.addAttribute("errorList", errors);
            return "validatedResult";
        } catch (IOException e) {
            e.printStackTrace();
        }
        String userPath = UPLOADED_FOLDER + req.getSession().getId() + "/";
        model.addAttribute("files", loadAll(Paths.get(userPath) ).map(
                path -> MvcUriComponentsBuilder.fromMethodName(ValidationXmlController.class,
                        "serveFile",  req.getSession().getId(), path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
        return "validateXml";
    }
    @RequestMapping("/files/{directory:.+}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String directory, @PathVariable String filename) {

        Resource file;
		try {
			file = loadAsResource(directory + "//" + filename);
	        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"").body(file);
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
                throw new Exception("Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new Exception("Could not read file: " + filename, e);
        }
    }
    public Path load(String filename) {
        return root.resolve(filename);
    }

    public Stream<Path> loadAll(Path root){
    	try {
			return Files.walk(root, 1).filter(path -> !path.equals(root)).map(root::relativize);
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Throwable e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleStorageFileNotFound(Exception exc) {
        return ResponseEntity.notFound().build();
    }
}

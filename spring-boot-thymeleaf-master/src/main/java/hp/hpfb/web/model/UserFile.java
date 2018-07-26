package hp.hpfb.web.model;

import org.springframework.web.multipart.MultipartFile;

public class UserFile {
	private MultipartFile  file;
	private Boolean local;
	private String version;

	public Boolean getLocal() {
		return local;
	}

	public void setLocal(Boolean local) {
		this.local = local;
	}

	public MultipartFile  getFile() {
		return file;
	}

	public void setFile(MultipartFile  file) {
		this.file = file;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}

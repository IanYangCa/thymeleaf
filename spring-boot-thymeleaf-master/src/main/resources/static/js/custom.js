
function onVersionChange(element, e){
	$('#filelist').empty();
	$.getJSON("/admin/stylesheet/" + $(element).val(), function(data, status) {
			if(data != null && data.length > 0){
				$.each(data, function(index, value){
					$('#filelist').append('<li><a href=' + value + '>' + value + '</a></li>');
				});
			}
		});
}
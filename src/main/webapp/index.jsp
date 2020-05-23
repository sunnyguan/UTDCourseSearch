<!DOCTYPE html>
<html lang="en">
<head>
<title>Spring MVC Async</title>
</head>
<body>
	<main>
	<h2>Spring MVC Async</h2>
	<table id="sse" style="width: 100%;" id="professors">
			<colgroup>
				<col span="1" style="width: 5%;">
				<col span="1" style="width: 8%;">
				<col span="1" style="width: 22%;">
				<col span="1" style="width: 10%;">
				<col span="1" style="width: 10%;">
				<col span="1" style="width: 10%;">
				<col span="1" style="width: 6%;">
				<col span="1" style="width: 9%;">
				<col span="1" style="width: 20%;">
			</colgroup>
			<thead>
				<tr data-sort-method="none"><th>Status</th>
				<th role="columnheader">Course</th>
				<th role="columnheader">Name</th>
				<th role="columnheader">Professor</th>
				<th role="columnheader">Rating</th>
				<th role="columnheader">Avg. GPA</th>
				<th role="columnheader" data-sort-default><div class="tooltip">Overall<span class="tooltiptext">30% RMP + 70% GPA</span></div></th>
				<th role="columnheader">Add Class (Not Galaxy)</th>
				<th role="columnheader">Schedule</th></tr>
			</thead>
		</table>
	</main>
</body>
<script>
	/**
	 * SSE
	 */
	var sse = new EventSource('feed');
	sse.onmessage = function(evt) {
		var data = evt.data;
		if(data !== done){
			var el = document.getElementById('sse');
			var z = document.createElement('tr');
			z.innerHTML = data;
			el.appendChild(z);
		} else {
			
		}
	};
</script>
</html>
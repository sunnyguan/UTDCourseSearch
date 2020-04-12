<html>
<head>
	<title>First JSP</title>
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/kognise/water.css@latest/dist/dark.min.css">
	<link href="main.css" rel="stylesheet" type="text/css">
	<script src="https://cdnjs.cloudflare.com/ajax/libs/tablesort/5.1.0/tablesort.min.js"></script>
	<script>
		function onPageReady() {
		  // Documentation: http://tristen.ca/tablesort/demo/
		  new Tablesort(document.getElementById('professors'));
		}

		// Run the above function when the page is loaded & ready
		document.addEventListener('DOMContentLoaded', onPageReady, false);
	</script>
</head>
<body>
  <form action="rmp">
  	<label>Course Search: </label>
  	<input type="text" name="course">
  	<input type="submit">
  </form>
  
  <p>${output}</p>
</body>
</html>
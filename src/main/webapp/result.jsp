<html>
<head>
<title>First JSP</title>
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/gh/kognise/water.css@latest/dist/dark.min.css">
<link href="main.css" rel="stylesheet" type="text/css">
<script src="js/tablesort.js"></script>
<script src="js/sorts/tablesort.number.js"></script>

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
	<h2>Roommate Search Results: </h2>
	<p>Time taken: ${time} ms, Number of Students in Database: ${numRoommates}</p>
	${output}
	
</body>
</html>
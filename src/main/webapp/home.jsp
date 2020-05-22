<html>
<head>
<title>${course} Results</title>
<!-- Global site tag (gtag.js) - Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=UA-167312978-1"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', 'UA-167312978-1');
</script>

<link rel="stylesheet" href="css/dark.min.css">
<link href="main.css" rel="stylesheet" type="text/css">
<link href="css/tablesort.css" rel="stylesheet" type="text/css">
<script src="js/tablesort.js"></script>
<script src="js/sorts/tablesort.number.js"></script>
<script src="js/jquery.js"></script>
<script>
	function onPageReady() {
		// Documentation: http://tristen.ca/tablesort/demo/
		new Tablesort(document.getElementById('professors'));
		
		$('#myonoffswitch').on('click', function () {
		    var $rowsNo = $('#professors tbody tr').filter(function () {
		        return $.trim($(this).find('td').eq(0).text()) === "Full";
		        
		    }).toggle();
		});
	}

	// Run the above function when the page is loaded & ready
	document.addEventListener('DOMContentLoaded', onPageReady, false);
</script>

</head>
<body>
	<form action="rmp">
		<select style="display:inline" class="term-popup" id="srch_term" name="term"><option value="term_all">Search All Terms</option><option value="_2020">---------------</option><option value="term_20f" selected="">2020 Fall (20f)</option><option value="term_20u">2020 Summer (20u)</option><option value="term_20s">2020 Spring (20s)</option><option value="_2019">---------------</option><option value="term_19f">2019 Fall (19f)</option><option value="term_19u">2019 Summer (19u)</option><option value="term_19s">2019 Spring (19s)</option><option value="_2018">---------------</option><option value="term_18f">2018 Fall (18f)</option><option value="term_18u">2018 Summer (18u)</option><option value="term_18s">2018 Spring (18s)</option><option value="_2017">---------------</option><option value="term_17f">2017 Fall (17f)</option><option value="term_17u">2017 Summer (17u)</option><option value="term_17s">2017 Spring (17s)</option><option value="_2016">---------------</option><option value="term_16f">2016 Fall (16f)</option><option value="term_16u">2016 Summer (16u)</option><option value="term_16s">2016 Spring (16s)</option><option value="_2015">---------------</option><option value="term_15f">2015 Fall (15f)</option><option value="term_15u">2015 Summer (15u)</option><option value="term_15s">2015 Spring (15s)</option><option value="_2014">---------------</option><option value="term_14f">2014 Fall (14f)</option><option value="term_14u">2014 Summer (14u)</option><option value="term_14s">2014 Spring (14s)</option><option value="_2013">---------------</option><option value="term_13f">2013 Fall (13f)</option><option value="term_13u">2013 Summer (13u)</option><option value="term_13s">2013 Spring (13s)</option><option value="_2012">---------------</option><option value="term_12f">2012 Fall (12f)</option><option value="term_12u">2012 Summer (12u)</option><option value="term_12s">2012 Spring (12s)</option><option value="_2011">---------------</option><option value="term_11f">2011 Fall (11f)</option><option value="term_11u">2011 Summer (11u)</option><option value="term_11s">2011 Spring (11s)</option><option value="_2010">---------------</option><option value="term_10f">2010 Fall (10f)</option><option value="term_10u">2010 Summer (10u)</option><option value="term_10s">2010 Spring (10s)</option></select>
		<input style="display:inline" type="text" name="course" placeholder="Course Name">
		<input style="display:inline"type="submit">
	</form>

	<h2>Search Result for: ${course}</h2>
	<p>Time taken: ${time} seconds, Number of Professors in Database: ${numProfs}</p>
    <p>Filter Open Classes Only:
    <div class="onoffswitch">
        <input type="checkbox" name="onoffswitch" class="onoffswitch-checkbox" id="myonoffswitch" tabindex="0" >
        <label class="onoffswitch-label" for="myonoffswitch">
            <span class="onoffswitch-inner"></span>
            <span class="onoffswitch-switch"></span>
        </label>
    </div>
    </p>
	${output}
	
</body>
</html>
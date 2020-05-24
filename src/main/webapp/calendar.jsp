
<html lang='en'>
  <head>
    <meta charset='utf-8' />
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <link href='fullcalendar/packages/core/main.css' rel='stylesheet' />
    <link href='fullcalendar/packages/daygrid/main.css' rel='stylesheet' />
    <link href='fullcalendar/packages/timegrid/main.css' rel='stylesheet' />

    <script src='fullcalendar/packages/core/main.js'></script>
    <script src='fullcalendar/packages/daygrid/main.js'></script>
    <script src='fullcalendar/packages/timegrid/main.js'></script>
	
	<style>
	html, body {
	  margin: 0;
	  padding: 0;
	  font-family: Arial, Helvetica Neue, Helvetica, sans-serif;
	  font-size: 14px;
	}
	
	#calendar {
	  max-width: 900px;
	  margin: 40px auto;
	}
	</style>

    <script>

    document.addEventListener('DOMContentLoaded', function() {
    	  var scr = ${events};
    	  $('body').append(scr);
    });
    </script>
  </head>
  <body>

    <div id='calendar'></div>

  </body>
</html>

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
	@import url(https://fonts.googleapis.com/css?family=Alegreya+Sans);
	html, body {
	  margin: 0;
	  padding: 0;
	  font-family: 'Alegreya Sans';	
	  font-size: 14px;
	  background-color: grey;
	}
	
	#calendar {
	  max-width: 900px;
	  margin: 40px auto;
	}
	</style>

    <script>

    document.addEventListener('DOMContentLoaded', function() {
		var calendarEl = document.getElementById('calendar');
		var calendar = new FullCalendar.Calendar(calendarEl, {
			plugins: [ 'timeGrid' ], 
			defaultView: 'timeGridWeek',
			defaultDate: '2020-04-20',
			customButtons: {
				refresh: {
					text: 'refresh',
					click: function() { 
						window.location.reload(true); 
					} 
				} 
			},
			header: {
				left: 'prev,next,refresh', 
				center: 'title',
				right: 'timeGridWeek'
			}
		});
		calendar.render();
		
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				// console.log(xhttp.responseText);
				calendar.addEventSource(JSON.parse(xhttp.responseText));
			}
		};
		xhttp.open("GET", "get_calendar", true);
		xhttp.send();
    });
    </script>
  </head>
  <body>

    <div id='calendar'></div>

  </body>
</html>
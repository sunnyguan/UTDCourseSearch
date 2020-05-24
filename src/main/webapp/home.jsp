<html>
	<head>
		<link rel="apple-touch-icon" sizes="180x180"
			href="fav/apple-touch-icon.png">
		<link rel="icon" type="image/png" sizes="32x32"
			href="fav/favicon-32x32.png">
		<link rel="icon" type="image/png" sizes="16x16"
			href="fav/favicon-16x16.png">
		<link rel="manifest" href="fav/site.webmanifest">
		<title>${course} Results</title>
		<!-- Global site tag (gtag.js) - Google Analytics -->
		<script async
			src="https://www.googletagmanager.com/gtag/js?id=UA-167312978-1"></script>
		<script>
			window.dataLayer = window.dataLayer || [];
			function gtag() {
				dataLayer.push(arguments);
			}
			gtag('js', new Date());
			gtag('config', 'UA-167312978-1');
		</script>
		<link rel="stylesheet" href="css/dark.min.css">
		<link href="main.css" rel="stylesheet" type="text/css">
		<link href="css/tablesort.css" rel="stylesheet" type="text/css">
		<script src="js/tablesort.js"></script>
		<script src="js/sorts/tablesort.number.js"></script>
		<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
		<link rel="stylesheet" href="magnific-popup/magnific-popup.css">
		<script src="magnific-popup/jquery.magnific-popup.min.js"></script>
		<script>
			var startTime = 0;
			var timer;
			var diff = 0;
			var sort;
			var popup_mode = false;
			var modal;
			jQuery( document ).ready(function( $ ) {

				var perfEntries = performance.getEntriesByType("navigation");

				if (perfEntries[0].type === "back_forward") {
				    location.reload(true);
				}

			});
			function onPageReady() {
			
				/**
				 * SSE
				 */
				var sse = new EventSource('feed');
				var ind = 0;
				sse.onmessage = function(evt) {
					var data = evt.data;
					diff = Math.round((Date.now() - startTime) * 10) / 10000;
					$('#time').text(diff);
					if (data !== "done") {
						$('#loading>h2').text("Retrieving Results #" + ind++);
						var el = document.getElementById('bod');
						var z = document.createElement('tr');
						z.innerHTML = data;
						el.appendChild(z);
						// if(ind % 20 == 0) sort.refresh();
					} else {
						$('#loading>h2').text(
								"Found " + ind + " results in " + diff + " seconds.");
						// setTimeout(function(){ $('#loading').hide(); }, 2000);
						// Documentation: http://tristen.ca/tablesort/demo/
						clearInterval(timer);
						sort.refresh();
						change_mode(popup_mode);
						
						$('.add').each(function() {
							var add = $(this);
							$('.drop').each(function() {
								if($(this).attr('value') === add.attr('value')){
									add.text('Remove');
									add.attr('href', '#');
									add.removeAttr('onclick');
									add.css('text-decoration', 'none');
								}
							});
							// console.log($(this).attr('value'));
						});
						rearrange_colors();
					}
					// var diff = Math.round((Date.now() - startTime) * 1000) / 1000000;
					// $('#time').text(diff);
				
				};
				
				modal = document.getElementById("myModal");
				var btn = document.getElementById("myBtn");
				var span = document.getElementsByClassName("close")[0];
				btn.onclick = function() {
					modal.style.display = "block";
				}
				span.onclick = function() {
					modal.style.display = "none";
				}
				window.onclick = function(event) {
					if (event.target == modal) {
						modal.style.display = "none";
					}
				}
			
				sort = new Tablesort(document.getElementById('professors'), {
					descending : true
				});
				startTime = Date.now();
				/*timer = setInterval(function() {
					diff = Math.round((Date.now() - startTime) * 10) / 10000;
					$('#time').text(diff);
				}, 100);*/
			
				$('#myonoffswitch3').on('click', function() {
					popup_mode = !popup_mode;
					change_mode(popup_mode);
				});
			
				$('#myonoffswitch2').on('click', function() {
					$('td:nth-child(9)').toggle();
					$('th:nth-child(9)').toggle();
				});
			
				$('#myonoffswitch').on('click', function() {
					var $rowsNo = $('#professors tbody tr').filter(function() {
						return $.trim($(this).find('td').eq(0).text()) === "Full";
			
					}).toggle();
					$('tbody tr:visible').each(function(index) {
						if (index % 2 == 1)
							$(this).css({
								"background-color" : "#161f27"
							});
						else
							$(this).css({
								"background-color" : "#202b38"
							});
					});
				});
			
				$('th').on('click', rearrange_colors);
			}
			
			function rearrange_colors(){
				$('tbody tr:visible').each(function(index) {
					if (index % 2 == 1)
						$(this).css({
							"background-color" : "#161f27"
						});
					else
						$(this).css({
							"background-color" : "#202b38"
						});
				});
			}
			
			function change_mode(popup) {
			
				$('.popup_grade, .popup_rmp, .popup_details, .popup_schedule')
						.each(function() {
						var hrf = $(this).attr("href").replace(/\s/g, "%20");
						if (popup) {
							$('#column2').hide();
							$('#column2>iframe').attr('src', "");
							$('#column1').css({
								'width' : '100%'
							});
							$('#column2').css({
								'width' : '0%'
							});
							$(this).unbind('click');
							$(this).magnificPopup({
								type : 'iframe',
								iframe : {
									markup : '<div class="mfp-iframe-scaler">'
											+ '<div class="mfp-close"></div>'
											+ '<iframe style="height: 600px;" src=' + hrf + '></iframe>'
								}
							});
						} else {
							$(this).unbind('click');
							$(this).click(function(event) {
								event.preventDefault();
								$.magnificPopup.close();
								if ($('#column2').is(':visible')) {
									if ($('#column2>iframe') .attr('src') === hrf) {
										$('#column2') .slideUp();
										$('#column2>iframe').attr('src',"");
										$('#column1').css({'width' : '100%'});
										$('#column2').css({'width' : '0%'});
									} else {
										$('#column2').html('<iframe style="height: 100%; width:100%" src=' + hrf + '></iframe>');
									}
								} else {
									$('#column2').html('<iframe style="height: 100%; width:100%" src=' + hrf + '></iframe>');
									$('#column2').show();
									$('#column1').css({'width' : '60%'});
									$('#column2').css({'width' : '40%'});
								}
							});
						}
					});
				}
			
			document.addEventListener('DOMContentLoaded', onPageReady, false);
			
			function toggle_sidebar(event, hrf){
				
			}
			
			function loadDoc() {
				var xhttp = new XMLHttpRequest();
				xhttp.onreadystatechange = function() {
					if (this.readyState == 4 && this.status == 200) {
						document.getElementById("search_history").innerHTML = "";
					}
				};
				xhttp.open("GET", "clear_history", true);
				xhttp.send();
			}
			function addCourse(element) {
				$.ajax({
					type : "GET",
					url : 'add_course',
					data : {
						"course" : $(element).attr('value')
					},
					success : function(data) {
						$('.dropdown-content').html(data);
						$(element).text('Remove');
						$(element).attr('onclick', 'removeCourse(this)');
					}
				});
			}
			function removeCourse(element) {
				$.ajax({
					type : "GET",
					url : 'remove_course',
					data : {
						"course" : $(element).attr('value')
					},
					success : function(data) {
						var deleted = data.split("##")[0];
						data = data.split("##")[1];
						$('.dropdown-content').html(data);
						$('.add').each(function() {
							if($(this).attr('value') === deleted){
								$(this).attr('onclick', 'addCourse(this)');
								$(this).text('Add');
							}
						});
					}
				});
			}
			
			function feedback(element) {
				$.ajax({
					type : "GET",
					url : 'feedback',
					data : {
						"info" : $("#feed").val()
					},
					success : function(data) {
						$('#feedback_info').text(data);
						$("#feed").val('');
						setTimeout(function() {
							modal.style.display = 'none';
							$('#feedback_info').text('');
						}, 1000);
					}
				});
			}
			
		</script>
	</head>
	<body>
		<div class="row">
			<div id="column1">
				<form action="rmp">
					<select style="display: inline" class="term-popup" id="srch_term"
						name="term">
						<option value="term_all">Search All Terms</option>
						<option value="_2020">---------------</option>
						<option value="term_20f" selected="">2020 Fall (20f)</option>
						<option value="term_20u">2020 Summer (20u)</option>
						<option value="term_20s">2020 Spring (20s)</option>
						<option value="_2019">---------------</option>
						<option value="term_19f">2019 Fall (19f)</option>
						<option value="term_19u">2019 Summer (19u)</option>
						<option value="term_19s">2019 Spring (19s)</option>
						<option value="_2018">---------------</option>
						<option value="term_18f">2018 Fall (18f)</option>
						<option value="term_18u">2018 Summer (18u)</option>
						<option value="term_18s">2018 Spring (18s)</option>
						<option value="_2017">---------------</option>
						<option value="term_17f">2017 Fall (17f)</option>
						<option value="term_17u">2017 Summer (17u)</option>
						<option value="term_17s">2017 Spring (17s)</option>
						<option value="_2016">---------------</option>
						<option value="term_16f">2016 Fall (16f)</option>
						<option value="term_16u">2016 Summer (16u)</option>
						<option value="term_16s">2016 Spring (16s)</option>
						<option value="_2015">---------------</option>
						<option value="term_15f">2015 Fall (15f)</option>
						<option value="term_15u">2015 Summer (15u)</option>
						<option value="term_15s">2015 Spring (15s)</option>
						<option value="_2014">---------------</option>
						<option value="term_14f">2014 Fall (14f)</option>
						<option value="term_14u">2014 Summer (14u)</option>
						<option value="term_14s">2014 Spring (14s)</option>
						<option value="_2013">---------------</option>
						<option value="term_13f">2013 Fall (13f)</option>
						<option value="term_13u">2013 Summer (13u)</option>
						<option value="term_13s">2013 Spring (13s)</option>
						<option value="_2012">---------------</option>
						<option value="term_12f">2012 Fall (12f)</option>
						<option value="term_12u">2012 Summer (12u)</option>
						<option value="term_12s">2012 Spring (12s)</option>
						<option value="_2011">---------------</option>
						<option value="term_11f">2011 Fall (11f)</option>
						<option value="term_11u">2011 Summer (11u)</option>
						<option value="term_11s">2011 Spring (11s)</option>
						<option value="_2010">---------------</option>
						<option value="term_10f">2010 Fall (10f)</option>
						<option value="term_10u">2010 Summer (10u)</option>
						<option value="term_10s">2010 Spring (10s)</option>
					</select>
					<input style="display: inline" type="text" name="course"
						placeholder="Course Name"> <input style="display: inline"
						type="submit">
					<div class="dropdown">
						<button type='button' class="dropbtn popup_schedule"
							href='/calendar' >Current Classes</button>
						<div class="dropdown-content">${classes}</div>
					</div>
				</form>
				<h2>Search Result for: ${course}</h2>
				<p>
					Number of Professors in Database: ${numProfs}, Time taken (seconds):
					<span id="time">${time}</span>
				</p>
				<div
					style="margin-bottom: 20px; display: inline-block; margin-right: 20px;">Filter
					Open Classes Only:
				</div>
				<div class="onoffswitch"
					style="height: 28px; display: inline-block; vertical-align: middle;">
					<input type="checkbox" name="onoffswitch"
						class="onoffswitch-checkbox" id="myonoffswitch" tabindex="0">
					<label class="onoffswitch-label" for="myonoffswitch"> <span
						class="onoffswitch-inner"></span> <span class="onoffswitch-switch"></span>
					</label>
				</div>
				<br>
				<div
					style="display: inline-block; margin-right: 15px; margin-bottom: 20px;">Toggle
					Schedule Column:
				</div>
				<div class="onoffswitch"
					style="height: 28px; display: inline-block; vertical-align: middle;">
					<input type="checkbox" name="onoffswitch2"
						class="onoffswitch-checkbox" id="myonoffswitch2" tabindex="0">
					<label class="onoffswitch-label" for="myonoffswitch2"> <span
						class="onoffswitch-inner"></span> <span class="onoffswitch-switch"></span>
					</label>
				</div>
				<br>
				<div
					style="display: inline-block; margin-right: 15px; margin-bottom: 20px;">Toggle
					Sidebar vs. Popup:
				</div>
				<div class="onoffswitch"
					style="height: 28px; display: inline-block; vertical-align: middle;">
					<input type="checkbox" name="onoffswitch3"
						class="onoffswitch-checkbox" id="myonoffswitch3" tabindex="0">
					<label class="onoffswitch-label" for="myonoffswitch3"> <span
						class="onoffswitch-inner"></span> <span class="onoffswitch-switch"></span>
					</label>
				</div>
				<div id="loading" style="text-align: center;">
					<h2>Retrieving from Coursebook...</h2>
				</div>
				<table style="width: 100%;" id="professors">
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
						<tr data-sort-method="none">
							<th>Status</th>
							<th role="columnheader">Course</th>
							<th role="columnheader">Name</th>
							<th role="columnheader">Professor</th>
							<th role="columnheader">Rating</th>
							<th role="columnheader">Avg. GPA</th>
							<th role="columnheader" data-sort-default>
								<div
									class="tooltip">
									Overall<span class="tooltiptext">30% RMP + 70% GPA</span>
								</div>
							</th>
							<th role="columnheader">Add to List</th>
							<th role="columnheader">Schedule</th>
						</tr>
					</thead>
					<tbody id="bod">
					</tbody>
				</table>
				<p>
					© Made by <a href="https://www.linkedin.com/in/sunny-guan/">Sunny
					Guan</a>, credit for UTDGrades visualization goes to <a
						href="https://www.linkedin.com/in/saitanayd/">Sai Desaraju</a>.
					Tools used: <a href="https://dimsemenov.com/plugins/magnific-popup/">magnific-popup</a>,
					<a href="https://kognise.github.io/water.css/">water.css</a>, <a
						href="http://tristen.ca/tablesort/demo/">tablesort</a>. <a
						id="myBtn">Feedback?</a>
				</p>
				<div id="myModal" class="modal">
					<div class="modal-content">
						<span>Feedback?</span> <span class="close">&times;</span>
						<textarea id="feed"></textarea>
						<button onclick="feedback(this);">Submit</button>
						<span id="feedback_info"></span>
					</div>
				</div>
			</div>
			<div id="column2"></div>
		</div>
		<footer>
			<p>
				<a id="clearHistory" onclick="loadDoc()">Clear</a>Search History: <span
					id="search_history"> ${history}</span>
			</p>
		</footer>
	</body>
</html>
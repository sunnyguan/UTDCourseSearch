var startTime = 0;
var timer;
var diff = 0;
var sort;
var popup_mode = false;
var modal;
var calendar;
jQuery(document).ready(function($) {

    var perfEntries = performance.getEntriesByType("navigation");

    if (perfEntries[0].type === "back_forward") {
        location.reload(true);
    }
    
});

/**
 * SSE
 */
var ind = 0;
var sse;
function new_feed() {
    sse = new EventSource('feed');
    console.log('new sse created');
    sse.onerror = function (e) {
	// sse.close();
	// console.log('error.');
    };
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
            // clearInterval(timer);
            sort.refresh();
            change_mode(popup_mode);
            
            $('.history_click').each( function() {
                $(this).on('click', make_click);
            });

            $('.add').each(function() {
                var add = $(this);
                $('.drop').each(function() {
                    if ($(this).attr('value') === add.attr('value')) {
                        add.text('Remove');
                        add.attr('href', '#');
                        add.removeAttr('onclick');
                        add.css('text-decoration', 'none');
                    }
                });
                // console.log($(this).attr('value'));
            });
            rearrange_colors();
            if(no_full) {
        	var $rowsNo = $('#professors tbody tr').filter(function() {
                    return $.trim($(this).find('td').eq(0).text()) === "Full";
                }).toggle();
                rearrange_colors();
            }
            
            console.log('sse closed');
            sse.close();
        }
        // var diff = Math.round((Date.now() - startTime) * 1000) / 1000000;
        // $('#time').text(diff);

    };
}

function make_click() {
    new_feed();
    startTime = Date.now();
    ind = 0;
    var req = $(this).attr('course');
    $('#search_string').text('Search Result for: ' + req);
    document.title = req;
    $.ajax({
        type: "GET",
        url: 'get_courses',
        data: {
            "course": req,
            "term": $(this).attr('term')
        },
        success: function(data) {
            // console.log(data);
            $('#loading>h2').text('Retrieving Coursebook...');
            $("#professors > tbody").html("");
            $("[name='course']").val('');
            $('#search_history').html(data);
        }
    });
}
function refresh_calendar() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            // console.log(xhttp.responseText);
            if (calendar !== undefined) {
                calendar.removeAllEvents();
                calendar.addEventSource(JSON.parse(xhttp.responseText));
            }
            $('.fc-time-grid-event-inset').each(function() {
                $(this).css('background-color', 'red');
            });
        }
    };
    xhttp.open("GET", "get_calendar", true);
    xhttp.send();
}

function submit_ajax(){
    new_feed();
    startTime = Date.now();
    ind = 0;
    var req = $("[name='course']").val();
    $('#search_string').text('Search Result for: ' + req);
    document.title = req;
    $.ajax({
        type: "GET",
        url: 'get_courses',
        data: {
            "course": req,
            "term": $("[name='term']").val()
        },
        success: function(data) {
            // console.log(data);
            $('#loading>h2').text('Retrieving Coursebook...');
            $("#professors > tbody").html("");
            $("[name='course']").val('');
            $('#search_history').html(data);
            
            $('.history_click').each( function() {
                $(this).on('click', make_click);
            });
        }
    });
}

function onPageReady() {
    new_feed();
    
    $("[name='course']").on('keypress',function(e) {
        if(e.which == 13) {
            submit_ajax();
        }
    });
    
    $('#request_courses').on('click', submit_ajax);

    $('#open_schedule')
        .on(
            'click',
            function() {
                if ($('#column2').is(':visible') &&
                    $('#column2').has('#calendar').length != 0) {
                    $('#column2').hide();
                    $('#column1').css({
                        'width': '100%'
                    });
                    $('#column2').css({
                        'width': '0%'
                    });
                } else {
                    $('#column2').html('<div id="calendar"></div>');
                    $('#column2').show();
                    $('#column1').css({
                        'width': '60%'
                    });
                    $('#column2').css({
                        'width': '40%'
                    });

                    var calendarEl = document
                        .getElementById('calendar');
                    calendar = new FullCalendar.Calendar(
                        calendarEl, {
                            plugins: ['timeGrid'],
                            defaultView: 'timeGridWeek',
                            defaultDate: '2020-04-20',
                            height: "parent",
                            header: {
                                left: 'prev,next',
                                center: 'title',
                                right: 'timeGridWeek'
                            },
                            visibleRange: {
                                start: '2020-04-20T09:00:00',
                                end: '2020-04-26T23:00:00'
                            },
                            eventRender: function(info) {
                                var style = 'display: none; z-index: 1000; color: black; position: absolute; top: 0; right: 0; width: 13px; height: 13px; text-align: center; border-radius: 50%; font-size: 10px; cursor: pointer; background-color: #FFF;';
                                $(info.el)
                                    .append(
                                        "<span style='" +
                                        style +
                                        "' class='removebtn'>X</span>");
                                $(info.el)
                                    .on({
                                        mouseenter: function() {
                                            $(this)
                                                .find(
                                                    '.removebtn')
                                                .eq(
                                                    0)
                                                .css(
                                                    'display',
                                                    'block');
                                        },
                                        mouseleave: function() {
                                            $(this)
                                                .find(
                                                    '.removebtn')
                                                .eq(
                                                    0)
                                                .css(
                                                    'display',
                                                    'none');
                                        }
                                    });
                                $(info.el)
                                    .find(".removebtn")
                                    .click(
                                        function() {
                                            var removeInfo = info.event.extendedProps.tag;
                                            $
                                                .ajax({
                                                    type: "GET",
                                                    url: 'remove_course',
                                                    data: {
                                                        "course": removeInfo
                                                    },
                                                    success: function(
                                                        data) {
                                                        var deleted = data
                                                            .split("##")[0];
                                                        data = data
                                                            .split("##")[1];
                                                        $(
                                                                '.dropdown-content')
                                                            .html(
                                                                data);
                                                        $(
                                                                '.add')
                                                            .each(
                                                                function() {
                                                                    if ($(
                                                                            this)
                                                                        .attr(
                                                                            'value') === deleted) {
                                                                        $(
                                                                                this)
                                                                            .attr(
                                                                                'onclick',
                                                                                'addCourse(this)');
                                                                        $(
                                                                                this)
                                                                            .text(
                                                                                'Add');
                                                                    }
                                                                });
                                                        refresh_calendar();
                                                    }
                                                });
                                        });
                            }
                        });
                    calendar.render();
                    refresh_calendar();
                }
            });
    

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
        descending: true
    });
    startTime = Date.now();
    /*
     * timer = setInterval(function() { diff = Math.round((Date.now() -
     * startTime) * 10) / 10000; $('#time').text(diff); }, 100);
     */

    $('#myonoffswitch3').on('click', function() {
        popup_mode = !popup_mode;
        change_mode(popup_mode);
    });

    $('#myonoffswitch2').on('click', function() {
        $('td:nth-child(9)').toggle();
        $('th:nth-child(9)').toggle();
    });

    $('#myonoffswitch').on('click', function (){
        no_full = !no_full;
        var $rowsNo = $('#professors tbody tr').filter(function() {
            return $.trim($(this).find('td').eq(0).text()) === "Full";
        }).toggle();
        rearrange_colors();
    });

    $('th').on('click', rearrange_colors);
    
    console.log($('.histry_click').length);
    
}

var no_full = false;

function rearrange_colors() {
    $('#bod>tr:visible').each(function(index) {
        if (index % 2 == 1)
            $(this).css({
                "background-color": "#161f27"
            });
        else
            $(this).css({
                "background-color": "#202b38"
            });
    });
}

function change_mode(popup) {

    $('.popup_grade, .popup_rmp, .popup_details')
        .each(
            function() {
                var hrf = $(this).attr("href").replace(/\s/g, "%20");
                if (popup) {
                    $('#column2').hide();
                    $('#column2>iframe').attr('src', "");
                    $('#column1').css({
                        'width': '100%'
                    });
                    $('#column2').css({
                        'width': '0%'
                    });
                    $(this).unbind('click');
                    $(this)
                        .magnificPopup({
                            type: 'iframe',
                            iframe: {
                                markup: '<div class="mfp-iframe-scaler">' +
                                    '<div class="mfp-close"></div>' +
                                    '<iframe style="height: 600px;" src=' +
                                    hrf +
                                    '></iframe>'
                            }
                        });
                } else {
                    $(this).unbind('click');
                    $(this)
                        .click(
                            function(event) {
                                event.preventDefault();
                                $.magnificPopup.close();
                                if ($('#column2')
                                    .is(':visible')) {
                                    if ($('#column2>iframe')
                                        .attr('src') === hrf) {
                                        $('#column2').slideUp();
                                        $('#column2>iframe')
                                            .attr('src', "");
                                        $('#column1').css({
                                            'width': '100%'
                                        });
                                        $('#column2').css({
                                            'width': '0%'
                                        });
                                    } else {
                                        $('#column2')
                                            .html(
                                                '<iframe style="height: 100%; width:100%" src=' +
                                                hrf +
                                                '></iframe>');
                                    }
                                } else {
                                    $('#column2')
                                        .html(
                                            '<iframe style="height: 100%; width:100%" src=' +
                                            hrf +
                                            '></iframe>');
                                    $('#column2').show();
                                    $('#column1').css({
                                        'width': '60%'
                                    });
                                    $('#column2').css({
                                        'width': '40%'
                                    });
                                }
                            });
                }
            });
}

document.addEventListener('DOMContentLoaded', onPageReady, false);

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
        type: "GET",
        url: 'add_course',
        data: {
            "course": $(element).attr('value')
        },
        success: function(data) {
            $('.dropdown-content').html(data);
            $(element).text('Remove');
            $(element).attr('onclick', 'removeCourse(this)');
            refresh_calendar();
        }
    });
}

function removeCourse(element) {
    $.ajax({
        type: "GET",
        url: 'remove_course',
        data: {
            "course": $(element).attr('value')
        },
        success: function(data) {
            var deleted = data.split("##")[0];
            data = data.split("##")[1];
            $('.dropdown-content').html(data);
            $('.add').each(function() {
                if ($(this).attr('value') === deleted) {
                    $(this).attr('onclick', 'addCourse(this)');
                    $(this).text('Add');
                }
            });
            refresh_calendar();
        }
    });
}

function feedback(element) {
    $.ajax({
        type: "GET",
        url: 'feedback',
        data: {
            "info": $("#feed").val()
        },
        success: function(data) {
            $('#feedback_info').text(data);
            $("#feed").val('');
            setTimeout(function() {
                modal.style.display = 'none';
                $('#feedback_info').text('');
            }, 1000);
        }
    });
}
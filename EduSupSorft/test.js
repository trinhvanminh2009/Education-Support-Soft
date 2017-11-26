// Initialize Firebase
var count = 0;
var widthImg = 0;
var heightImg = 0;
var rotate = '';
var zoom = 1;
function toggleFullScreen(elem) {
    // ## The below if statement seems to work better ## if ((document.fullScreenElement && document.fullScreenElement !== null) || (document.msfullscreenElement && document.msfullscreenElement !== null) || (!document.mozFullScreen && !document.webkitIsFullScreen)) {
    if ((document.fullScreenElement !== undefined && document.fullScreenElement === null) || (document.msFullscreenElement !== undefined && document.msFullscreenElement === null) || (document.mozFullScreen !== undefined && !document.mozFullScreen) || (document.webkitIsFullScreen !== undefined && !document.webkitIsFullScreen)) {
        if (elem.requestFullScreen) {
            elem.requestFullScreen();
        } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
        } else if (elem.webkitRequestFullScreen) {
            elem.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
        } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
        }
        //style full screen
        $('#myModal').addClass('full'); modal
    } else {
        if (document.cancelFullScreen) {
            document.cancelFullScreen();
        } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        } else if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
        $('#myModal').attr('class', 'modal')
    }

}
var config = {
    apiKey: "AIzaSyBq4pId3cpVHtQder1BLOGzjUo22V0po4s",
    authDomain: "edusupportsoft.firebaseapp.com",
    databaseURL: "https://edusupportsoft.firebaseio.com",
    projectId: "edusupportsoft",
    storageBucket: "edusupportsoft.appspot.com",
    messagingSenderId: "1077343274941"
};
firebase.initializeApp(config);
//get images list from realtime-database

var database = firebase.database();
var commentsRef = firebase.database().ref('/Lab1');
var arrData = [];
commentsRef.on('child_added', function (data) {
    //this is data, when firebase recieved an image, this function will call
    var id = data.key;
    var info = data.val();
    arrData.push({ id: data.key, info: data.val() })
    var card = '<div class="card">'
        + '<img id="myImg' + id + '" src= "' + info.bitmapUrl + '" style= "cursor: pointer;max-height: 250px;"  >'
        + '<p class="card-text">Name: ' + info.whoDidIt + ' </p>'
        + '<p class="card-text">ID: ' + id + ' </p>'
        + '<p class="card-text">Score:  </p></div >'
    $('.album.text-muted .container .row').prepend(card)
    var modal = '<div class="mySlides" >'
        + '<div class="numbertext" > Name</div >'
        + '<img src="' + info.bitmapUrl + '" class="cimg"></div>';
    $('#myModal .modal-content').append(modal)

    var modal = document.getElementById('myModal');

    // Get the image and insert it inside the modal - use its "alt" text as a caption
    var img = document.getElementById('myImg' + id);
    var modalImg = document.getElementById("img01");
    var captionText = document.getElementById("caption");
    img.onclick = function () {
        modal.style.display = "block";
        modalImg.src = this.src;
        captionText.innerHTML = info.whoDidIt + ' - ' + id;
        $('#wraper').css('width', modalImg.width + 'px');
        $("#myModal img.modal-content").css('transform', 'rotate(0deg)')
        var native_width = 0;
        var native_height = 0;
        count = 0;
        rotate = $('img.modal-content').attr('style');
        widthImg = $('img.modal-content').width();
        heightImg = $('img.modal-content').height();
    }
    // Get the <span> element that closes the modal
    var span = document.getElementsByClassName("closes")[0];
    // When the user clicks on <span> (x), close the modal
    span.onclick = function () {
        modal.style.display = "none";
        if ((document.fullScreenElement === undefined && document.fullScreenElement !== null) || (document.msFullscreenElement === undefined && document.msFullscreenElement !== null) || (document.mozFullScreen === undefined && document.mozFullScreen) || (document.webkitIsFullScreen === undefined && document.webkitIsFullScreen)) {
            if (document.cancelFullScreen) {
                document.cancelFullScreen();
            } else if (document.mozCancelFullScreen) {
                document.mozCancelFullScreen();
            } else if (document.webkitCancelFullScreen) {
                document.webkitCancelFullScreen();
            } else if (document.msExitFullscreen) {
                document.msExitFullscreen();
            }
            $('#myModal').attr('class', 'modal')
        }
    }
});

$('#txtSearch').on('input', function (e) {
    var keyword = $(this).val();
    if (!keyword) return $('html, body').animate({
        scrollTop: 0
    }, 500, function () {
        // Add hash (#) to URL when done scrolling (default click behavior)
        window.location.hash = 0;
    });
    var obj = arrData.find(function (e) {
        return e.id.indexOf(keyword) > -1 || e.info.whoDidIt.indexOf(keyword) > -1
    })
    if (!obj) return;
    // console.log($('#myImg' + obj.id).offset().top)
    $('html, body').animate({
        scrollTop: $('#myImg' + obj.id).offset().top - 100
    }, 500, function () {

        // Add hash (#) to URL when done scrolling (default click behavior)
        window.location.hash = $('#myImg' + obj.id);
    });
})


function zoomIn() {
    zoom += 0.2;
    $('img.modal-content').css('cssText', rotate + 'transform: scale(' + zoom + ')')
    var currentHeight = $('img.modal-content').height();
    $('img.modal-content').draggable({
        refreshPositions: true
    })
    // $('img.modal-content').draggable({
    //     start: function (event, ui) {
    //         if(ui.offset.top < 0)
                
    //     }
    // });
}
function zoomOut() {
    // if (widthImg === $('img.modal-content').width()) return;
    if(zoom === 1) return;
    rotate = $('img.modal-content').attr('style');
    zoom -= 0.2;
    $('img.modal-content').css('cssText', rotate + 'transform: scale(' + zoom + ')')
    $('img.modal-content').draggable()
}

$("body").on("click", "#rotate-left", function () {
    count -= 90
    $("#myModal img.modal-content").css('transform', 'rotate(' + count + 'deg)')
});
$("body").on("click", "#rotate-right", function () {
    count += 90
    $("#myModal img.modal-content").css('transform', 'rotate(' + count + 'deg)')
});

function onSub(e) {
    e.preventDefault();
}
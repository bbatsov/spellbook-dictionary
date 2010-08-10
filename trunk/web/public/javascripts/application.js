/*
var timeout         = 500;
var closetimer      = 0;
var ddmenuitem      = 0;

function nav_open()
{	nav_canceltimer();
	nav_close();
	ddmenuitem = jQuery(this).find('ul').eq(0).css('visibility', 'visible');}

function nav_close()
{	if(ddmenuitem) ddmenuitem.css('visibility', 'hidden');}

function nav_timer()
{	closetimer = window.setTimeout(nav_close, timeout);}

function nav_canceltimer()
{	if(closetimer)
	{	window.clearTimeout(closetimer);
		closetimer = null;}}

jQuery(document).ready(function()
{	jQuery('#nav > li').bind('mouseover', nav_open);
	jQuery('#nav > li').bind('mouseout',  nav_timer);});

document.onclick = nav_close;
*/

function clearText(field){
    if (field.defaultValue == field.value) field.value = '';
    else if (field.value == '') field.value = field.defaultValue;
}

function resize(){
    var page = document.getElementById("page");
    //var footer = document.getElementById("footer");
    var wrapper = document.getElementById("wrapper");
    var container = document.getElementById("container");

    var pageHeight = container.clientHeight - wrapper.offsetHeight - 40;
    page.style.minHeight = pageHeight + "px";
}

jQuery(document).ready(function ()
{
    jQuery('.main li:has(ul) > a').addClass('more'); //Tursi vsqko LI, v koeto ima UL i LI e roditelski element na A. Ako sa izpylneni usloviqta, dobavq na A class "more". Taka ako v LI imame UL, shte stava qsno, che imame submenu i shte podtikvame user-a da click-a.
    jQuery('a.more').append('<span class="arrow">&nbsp;&nbsp;&raquo;</span>'); //S tozi red dobavqme strelkichki na vsqko A, koeto ima class "more".
    jQuery('.main li').hover(function () {
            jQuery(this).find('ul:first').stop(true, true).animate({opacity: 'toggle', height: 'toggle'}, 200).addClass('active_list');
    }, function () {
            jQuery(this).children('ul.active_list').stop(true, true).animate({opacity: 'toggle', height: 'toggle'}, 200).removeClass('active_list');
    });	 // Gornite nqkolko reda predstavlqvat effecta na slide up & down. Vajno e da go zadadem za vsqko pyrvo UL. V red 6-ti tyrsim pyrvoto UL, koeto se namira w .MAIN LI i mu puskame animaciq, koqto go slide-va nadolu i mu dobavq class "active_list". V red 8 (koito predstavlqva hover out), tyrsim UL, koeto veche ima class "active_list" i kazvame ako mishoka ne e vyrhu nego da se izpylni animaciqta i da se premahne class-a "active_list". Po tozi nachin ako mrydnem mishkata vstrani menu-to shte se zatvori.
});
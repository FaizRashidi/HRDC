$(function(){
    if ($("#form-canvas").length > 0) {
        $("#form-canvas").find(".form-cell, .subform-cell").each(function(){
            var label = $(this).find("> label.label").text();
            $(this).find("> textarea, > input, > select").attr("placeholder", label);
        });
    }
});
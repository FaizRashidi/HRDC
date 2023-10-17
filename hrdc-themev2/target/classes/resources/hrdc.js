const header = `
                <div class="main-header"">
                    <img src="https://hrdcorp.gov.my/wp-content/uploads/2021/02/MoHR_HRDCorp_logo_website.svg">
                </div>`


const applyCss = (mutationList) => {
    let table = $('.table-wrapper table')
    let filter = $('.filter_form[id^=filters]')
    let footer = $('.table-wrapper').parent()
    let tableContainer = $('.table-mari')
    let sidebarLogo = $(".sidebar_brand")
    
    let filterHeight, footerHeight, deductHeight
    
    if (mutationList.length > 0) {
        if(tableContainer.length == 0){
            //$(filter).after('<div class="table-mari"></div>')
            //$(table).appendTo('.table-mari')
            
            setTimeout(function(){
                
                if($(filter).length > 0){
                    filterHeight = $(filter).height()
                }

                if($(footer).length > 0){
                    footerHeight = $(footer).height()
                }

                deductHeight = filterHeight + footerHeight + 333
                
                
                $('td.column_body p[style*="background-color: GREEN"]').css({"color": "green", "background": "#ccf4cc"})
                $('td.column_body p[style*="background-color: YELLOW"]').css({"color": "#ffa705", "background": "#fbfb8c"})
                $('td.column_body p[style*="background-color: GREY"]').css({"color": "#585858", "background": "#e6e2e2"})
                $('td.column_body p[style*="background-color: RED"]').css({"color": "red", "background": "#ffc3c3"})

            }, 150)
            
            
        }
      
    }
}

const targetNode = document.getElementById("category-container");
const config = { attributes: true, childList: true, subtree: true };
const observer = new MutationObserver(applyCss);


$(function(){
    $('body:not(".popupBody"):not("#login")').prepend(header)
    
    if ($("#form-canvas").length > 0) {
        $("#form-canvas").find(".form-cell, .subform-cell").each(function(){
            var label = $(this).find("> label.label").text();
            $(this).find("> textarea, > input, > select").attr("placeholder", label);
        });
    }
    observer.observe(targetNode, config);
    
});
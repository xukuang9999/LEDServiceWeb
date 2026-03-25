
$(function() {
    validateRule();
    $('.imgcode').click(function() {
        var url = ctx + "captcha/captchaImage?type=" + captchaType + "&s=" + Math.random();
        $(".imgcode").attr("src", url);
    });
});

function register() {
    var username = $.common.trim($("input[name='username']").val());
    var password = $.common.trim($("input[name='password']").val());
    var validateCode = $("input[name='validateCode']").val();
    if($.common.isEmpty(validateCode) && captchaEnabled) {
        $.modal.msg("Please enter the verification code");
        return false;
    }
    $.ajax({
        type: "post",
        url: ctx + "register",
        data: {
            "loginName": username,
            "password": password,
            "validateCode": validateCode
        },
        beforeSend: function () {
            $.modal.loading($("#btnSubmit").data("loading"));
        },
        success: function(r) {
            if (r.code == web_status.SUCCESS) {
            	layer.alert("<font color='red'>Congratulate you，your account is " + username + " registered successful！</font>", {
            	    icon: 1,
            	    title: "system message"
            	},
            	function(index) {
            	    //关闭弹窗
            	    layer.close(index);
            	    location.href = ctx + 'login';
            	});
            } else {
            	$.modal.closeLoading();
            	$('.imgcode').click();
            	$(".code").val("");
            	$.modal.msg(r.msg);
            }
        }
    });
}

function validateRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#registerForm").validate({
        rules: {
            username: {
                required: true,
                minlength: 2
            },
            password: {
                required: true,
                minlength: 5,
                specialSign: true
            },
            confirmPassword: {
                required: true,
                equalTo: "[name='password']"
            }
        },
        messages: {
            username: {
                required: icon + "Please enter your user name",
                minlength: icon + "The user name can not be less than 2 characters"
            },
            password: {
            	required: icon + "Please enter your password",
                minlength: icon + "The password can not be less than 5 characters",
            },
            confirmPassword: {
                required: icon + "Please enter your password again",
                equalTo: icon + "The two passwords are inconsistent"
            }
        },
        submitHandler: function(form) {
            register();
        }
    })
}

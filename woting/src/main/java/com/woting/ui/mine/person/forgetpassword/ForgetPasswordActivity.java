package com.woting.ui.mine.person.forgetpassword;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.woting.R;
import com.woting.common.config.GlobalConfig;
import com.woting.common.util.DialogUtils;
import com.woting.common.util.ToastUtils;
import com.woting.common.volley.VolleyCallback;
import com.woting.common.volley.VolleyRequest;
import com.woting.ui.base.baseactivity.AppBaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 忘记密码
 * 作者：xinlong on 2016/7/19 21:18
 * 邮箱：645700751@qq.com
 */
public class ForgetPasswordActivity extends AppBaseActivity implements OnClickListener {
    private CountDownTimer mCountDownTimer;// 再次获取验证码时间

    private Dialog dialog;                 // 加载数据对话框
    private EditText editPhoneNum;         // 输入 手机号
    private EditText editPassWord;         // 输入 密码
    private EditText editPassWordqz;       // 确认 密码
    private EditText editVerifyCode;       // 输入 验证码
    private TextView textGetVerifyCode;    // 获取验证码
    private TextView textCxFaSong;         // 重新获取验证码
    private TextView textConfirm;          // 确定

    private int sendType = 1;              // sendType == 1 首次获取验证码  sendType == 2 重发验证码
    private String phoneNum;               // 手机号
    private String password;               // 密码
    private String verifyCode;             // 验证码
    private String tag = "FORGET_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                finish();
                break;
            case R.id.tv_register:// 验证数据
                checkValue();
                break;
            case R.id.tv_getyzm:// 检查手机号是否为空或是否是一个正常手机号
                checkVerifyCode();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initView();
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);

        editPhoneNum = (EditText) findViewById(R.id.edittext_userphone);              // 输入 手机号
        editPassWord = (EditText) findViewById(R.id.edittext_password);               // 输入 密码
        editPassWordqz = (EditText) findViewById(R.id.edittext_passwordqz);           // 确认密码
        editVerifyCode = (EditText) findViewById(R.id.et_yzm);                        // 输入 验证码

        textGetVerifyCode = (TextView) findViewById(R.id.tv_getyzm);                  // 获取验证码
        textGetVerifyCode.setOnClickListener(this);
        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);                     // 再次获取验证码

        textConfirm = (TextView) findViewById(R.id.tv_register);                      // 确定
        textConfirm.setOnClickListener(this);
        setEditListener();                                                            // 输入框监听
    }

    private void setEditListener() {
        // 手机号输入框的监听
        editPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setBtView();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 密码输入框的监听
        editPassWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setBtView();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 确认密码输入框的监听
        editPassWordqz.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setBtView();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 验证码输入框的监听
        editVerifyCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setBtView();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void setBtView() {
        String phoneNum = editPhoneNum.getText().toString().trim();
        String password = editPassWord.getText().toString().trim();
        String password_qz = editPassWordqz.getText().toString().trim();
        String verifyCode = editVerifyCode.getText().toString().trim();

        if (phoneNum != null && !phoneNum.equals("")) {
            if (password != null && !password.equals("") && password.length() > 5) {
                if (password_qz != null && !password_qz.equals("") && password_qz.length() > 5) {
                    if (verifyCode != null && !verifyCode.equals("") && verifyCode.length() == 6) {
                        textConfirm.setBackgroundResource(R.drawable.zhuxiao_press);
                    } else {
                        textConfirm.setBackgroundResource(R.drawable.bg_graybutton);
                    }
                } else {
                    textConfirm.setBackgroundResource(R.drawable.bg_graybutton);
                }
            } else {
                textConfirm.setBackgroundResource(R.drawable.bg_graybutton);
            }
        } else {
            textConfirm.setBackgroundResource(R.drawable.bg_graybutton);
        }
    }

    // 检查手机号获取验证码
    private void checkVerifyCode() {
        phoneNum = editPhoneNum.getText().toString().trim();
        if ("".equalsIgnoreCase(phoneNum) || !isMobile(phoneNum)) {
            ToastUtils.show_always(context, "请输入正确的手机号码!");
            return;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialog(context);
            sendFindPassword();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    // 检查数据的正确性 通过则进行修改密码请求
    private void checkValue() {
        verifyCode = editVerifyCode.getText().toString().trim();
        password = editPassWord.getText().toString().trim();
        String passwordqz = editPassWordqz.getText().toString().trim();
        if ("".equalsIgnoreCase(phoneNum) || !isMobile(phoneNum)) {
            ToastUtils.show_always(context, "请输入正确的手机号码!");
            return;
        }
        if ("".equalsIgnoreCase(verifyCode) || verifyCode.length() != 6) {
            ToastUtils.show_always(context, "请输入正确的验证码!");
            return;
        }
        if ("".equalsIgnoreCase(password) || password.length() != 6) {
            ToastUtils.show_always(context, "请输入长度六位以上的密码!");
            return;
        }
        if ("".equalsIgnoreCase(passwordqz) || passwordqz.length() != 6) {
            ToastUtils.show_always(context, "请输入长度六位以上的确认密码!");
            return;
        }
        if (!password.equalsIgnoreCase(passwordqz)) {
            ToastUtils.show_always(context, "您两次输入的密码不一样!");
            return;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialog(context);
            sendRequest();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    // 提交数据到服务器进行验证
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            jsonObject.put("CheckCode", verifyCode);
            jsonObject.put("NeedUserId", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.requestPost(GlobalConfig.checkPhoneCheckCodeUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String UserId = result.getString("UserId");
                            if (UserId != null && !UserId.equals("")) {
                                sendModifyPassword(UserId);// 进入 modifyPassword 修改当前 userId 的手机号
                            } else {
                                Log.e("checkPhoneCheckCodeUrl", "获取UserId异常");
                                ToastUtils.show_always(context, "出错了，请稍后再试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("checkPhoneCheckCodeUrl", "获取UserId异常");
                            ToastUtils.show_always(context, "出错了，请稍后再试");
                        }
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "验证码不匹配");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.show_always(context, "出错了，请稍后再试");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtils.show_always(context, "出错了，请稍后再试");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 获取验证码
    private void sendFindPassword() {
        String url;
        if (sendType == 1) {
            url = GlobalConfig.retrieveByPhoneNumUrl;// 首次获取验证码
        } else {
            url = GlobalConfig.reSendPhoneCheckCodeNumUrl;// 再次获取验证码
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            if (sendType == 2) {
                jsonObject.put("OperType", "2");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.requestPost(url, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "验证码已经发送");
                        sendType = 2;
                        timerDown();
                        editPhoneNum.setEnabled(false);
                        textGetVerifyCode.setVisibility(View.GONE);
                        textCxFaSong.setVisibility(View.VISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "此手机号在系统内没有注册");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            } else {
                                ToastUtils.show_always(context, "出错了，请稍后再试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtils.show_always(context, "出错了，请稍后再试");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtils.show_always(context, "出错了，请稍后再试");
                }

            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 确定修改密码请求
    protected void sendModifyPassword(String userId) {
        dialog = DialogUtils.Dialog(context);
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("RetrieveUserId", userId);
            jsonObject.put("NewPassword", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.requestPost(GlobalConfig.updatePwd_AfterCheckPhoneOKUrl, userId, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "密码修改成功");
                        finish();
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        Log.e("修改密码", "0000——无法获取相关的参数");
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1000")) {
                        Log.e("修改密码", "1000——无法获取会话信息");
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        Log.e("修改密码", "1002——无法获取用户Id");
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        Log.e("修改密码", "1003——参数不合法");
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        Log.e("修改密码", "1004——存储新密码失败");
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1005")) {
                        Log.e("修改密码", "1005——状态错误");
                        ToastUtils.show_always(context, "出错了，请稍后再试");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            } else {
                                ToastUtils.show_always(context, "出错了，请稍后再试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtils.show_always(context, "出错了，请稍后再试");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 再次获取验证码时间
    private void timerDown() {
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeString = millisUntilFinished / 1000 + "s后重新发送";
                textCxFaSong.setText(timeString);
            }

            @Override
            public void onFinish() {
                textCxFaSong.setVisibility(View.GONE);
                textGetVerifyCode.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    // 验证手机号的方法
    private boolean isMobile(String str) {
        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        editPassWord = null;
        editVerifyCode = null;
        textGetVerifyCode = null;
        textCxFaSong = null;
        textConfirm = null;
        editPhoneNum = null;
        phoneNum = null;
        dialog = null;
        phoneNum = null;
        password = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }

}

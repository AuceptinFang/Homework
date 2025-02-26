import{d as e,r as l,o as a,a as t,z as i,c as o,b as d,w as u,e as r,f as n,E as s,h as m,v as c,k as p,l as f,m as v,n as b,A as g,x as w,u as y,g as h,q as _}from"./index-DsKRLMUq.js";const V={class:"admin-container"},k={class:"header-content"},x={class:"user-info"},S={class:"card-header"},C={class:"card-header"},B={class:"header-left"},U={class:"submission-stats",style:{"margin-top":"20px"}},I={class:"dialog-footer"},$={class:"dialog-footer"},F=_(e({__name:"Admin",setup(e){const _=y(),F=l(localStorage.getItem("username")||""),T=l(""),D=l(""),Y=l(!1),L=l(!1),M=l(null),R=l(null),q=l(),A=["admin","段鹏鹏","方震","高天全","高子涵","龚皓","霍雨婷","贾奇燠","贾一丁","金闿翎","靳宇晨","李希","刘森源","刘文晶","吕彦儒","吕泽熙","苏宗佑","汤锦健","王奕涵","王悦琳","王志成","王墨","王泽君","夏正鑫","肖俊波","杨海屹","杨孟涵","张成宇","张峻菘","张钟文","赵萌"],H=l([]),j=l([]);a((()=>{(async()=>{var e,l;try{const e=await n.get("/api/assignments");H.value=e.data}catch(a){s.error((null==(l=null==(e=a.response)?void 0:e.data)?void 0:l.error)||"获取作业列表失败")}})()}));const E=t({title:"",description:"",deadline:"",allowSubmit:!0,fileFormat:"pdf"}),z={title:[{required:!0,message:"请输入作业标题",trigger:"blur"},{min:2,max:50,message:"标题长度应在2到50个字符之间",trigger:"blur"}],description:[{required:!0,message:"请输入作业说明",trigger:"blur"}],deadline:[{required:!0,message:"请选择截止日期",trigger:"change"}]},O=l({content:""}),P=i((()=>{if(!R.value)return[];return A.filter((e=>"admin"!==e)).map((e=>{const l=j.value.find((l=>{const a="string"==typeof l.assignmentId?parseInt(l.assignmentId):l.assignmentId,t="string"==typeof R.value.id?parseInt(R.value.id):R.value.id;let i="string"==typeof l.submittedBy?l.submittedBy:"";return a===t&&i===e}));return l?{...l,submittedBy:e}:{id:0,assignmentId:R.value.id,title:R.value.title,submitTime:"-",status:"unsubmitted",file:null,submittedBy:e}})).filter((e=>{const l="string"==typeof e.submittedBy?e.submittedBy:e.submittedBy||"",a=""===T.value||l.toLowerCase().includes(T.value.toLowerCase()),t=""===D.value||e.status===D.value;return a&&t}))})),G=i((()=>P.value.filter((e=>"submitted"===e.status)).length)),J=i((()=>P.value.filter((e=>"unsubmitted"===e.status)).length)),K=i((()=>{const e=P.value.length;return e>0?Math.round(G.value/e*100):0})),N=e=>({submitted:"已提交",unsubmitted:"未提交"}[e]||"未知"),Q=()=>{w.confirm("确定要退出登录吗？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((()=>{localStorage.removeItem("token"),localStorage.removeItem("isAdmin"),localStorage.removeItem("username"),_.push("/")}))},W=()=>{E.title="",E.description="",E.deadline="",E.allowSubmit=!0,E.fileFormat="pdf",Y.value=!0},X=e=>e.getTime()<Date.now(),Z=async()=>{q.value&&await q.value.validate((async e=>{var l,a;if(e)try{const e=E.deadline?E.deadline.replace(" ","T")+":00":null,l=await n.post("/api/assignments",{title:E.title,description:E.description,deadline:e,allowSubmit:E.allowSubmit,fileFormat:E.fileFormat});H.value.unshift(l.data),s.success("作业发布成功！"),Y.value=!1}catch(t){s.error((null==(a=null==(l=t.response)?void 0:l.data)?void 0:a.error)||"发布作业失败，请检查网络连接")}}))},ee=async()=>{var e,l;if(O.value.content.trim()){if(M.value)try{const e=await n.put(`/api/submissions/${M.value.id}/feedback`,{feedback:O.value.content}),l=j.value.findIndex((e=>{var l;return e.id===(null==(l=M.value)?void 0:l.id)}));-1!==l&&(j.value[l]=e.data),s.success("反馈已提交"),L.value=!1}catch(a){s.error((null==(l=null==(e=a.response)?void 0:e.data)?void 0:l.error)||"提交反馈失败")}}else s.warning("请输入反馈内容")};return(e,l)=>{const a=r("el-button"),t=r("el-header"),i=r("el-table-column"),y=r("el-tag"),le=r("el-switch"),ae=r("el-table"),te=r("el-card"),ie=r("el-col"),oe=r("el-icon"),de=r("el-input"),ue=r("el-option"),re=r("el-select"),ne=r("el-descriptions-item"),se=r("el-descriptions"),me=r("el-row"),ce=r("el-main"),pe=r("el-container"),fe=r("el-form-item"),ve=r("el-date-picker"),be=r("el-radio"),ge=r("el-radio-group"),we=r("el-form"),ye=r("el-dialog");return h(),o("div",V,[d(pe,null,{default:u((()=>[d(t,null,{default:u((()=>[m("div",k,[l[13]||(l[13]=m("h2",null,"作业提交系统 - 管理员",-1)),m("div",x,[m("span",null,c(F.value),1),d(a,{type:"text",onClick:Q},{default:u((()=>l[12]||(l[12]=[p("退出登录")]))),_:1})])])])),_:1}),d(ce,null,{default:u((()=>[d(me,{gutter:20},{default:u((()=>[d(ie,{span:24,style:{"margin-bottom":"20px"}},{default:u((()=>[d(te,null,{header:u((()=>[m("div",S,[l[15]||(l[15]=m("span",null,"作业发布管理",-1)),d(a,{type:"primary",onClick:W},{default:u((()=>l[14]||(l[14]=[p(" 发布新作业 ")]))),_:1})])])),default:u((()=>[d(ae,{data:H.value,style:{width:"100%"}},{default:u((()=>[d(i,{prop:"title",label:"作业标题"}),d(i,{prop:"description",label:"作业说明","show-overflow-tooltip":""}),d(i,{prop:"deadline",label:"截止日期",width:"180"}),d(i,{prop:"createTime",label:"发布时间",width:"180"}),d(i,{label:"文件格式",width:"100"},{default:u((({row:e})=>[d(y,{type:"pdf"===e.fileFormat?"warning":"info"},{default:u((()=>[p(c("pdf"===e.fileFormat?"仅PDF":"不限"),1)])),_:2},1032,["type"])])),_:1}),d(i,{label:"提交控制",width:"120"},{default:u((({row:e})=>[d(le,{modelValue:e.allowSubmit,"onUpdate:modelValue":l=>e.allowSubmit=l,"active-text":"允许","inactive-text":"禁止",onChange:l=>(async e=>{var l,a;try{await n.put(`/api/assignments/${e.id}/submit-control`,{allowSubmit:e.allowSubmit});const l=e.allowSubmit?"允许":"禁止";s.success(`已${l}作业提交`)}catch(t){s.error((null==(a=null==(l=t.response)?void 0:l.data)?void 0:a.error)||"更新提交控制失败"),e.allowSubmit=!e.allowSubmit}})(e)},null,8,["modelValue","onUpdate:modelValue","onChange"])])),_:1}),d(i,{label:"操作",width:"180"},{default:u((({row:e})=>[d(a,{type:"primary",link:"",onClick:l=>(async e=>{var l;try{if(!(null==e?void 0:e.id))return void s.error("无效的作业ID");if(!localStorage.getItem("token"))return s.error("登录已过期，请重新登录"),void _.push("/");const l=await n.get(`/api/submissions/assignment/${e.id}`);j.value=l.data,R.value=e}catch(a){if(a.response){const e=a.response.status,t=(null==(l=a.response.data)?void 0:l.error)||"未知错误";400===e?s.error(`请求参数错误: ${t}`):401===e?(s.error("登录已过期，请重新登录"),_.push("/")):403===e?s.error("没有权限查看此作业的提交情况"):404===e?s.error("作业不存在"):s.error(`获取提交情况失败: ${t}`)}else s.error("网络错误，请检查网络连接")}})(e)},{default:u((()=>l[16]||(l[16]=[p(" 查看提交情况 ")]))),_:2},1032,["onClick"]),d(a,{type:"danger",link:"",onClick:l=>{return a=e,void w.confirm("确定要删除这个作业吗？","警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((async()=>{var e,l,t;try{await n.delete(`/api/assignments/${a.id}`),H.value=H.value.filter((e=>e.id!==a.id)),(null==(e=R.value)?void 0:e.id)===a.id&&(R.value=null),s.success("删除成功")}catch(i){s.error((null==(t=null==(l=i.response)?void 0:l.data)?void 0:t.error)||"删除作业失败")}}));var a}},{default:u((()=>l[17]||(l[17]=[p(" 删除 ")]))),_:2},1032,["onClick"])])),_:1})])),_:1},8,["data"])])),_:1})])),_:1}),d(ie,{span:24},{default:u((()=>[R.value?(h(),f(te,{key:0},{header:u((()=>[m("div",C,[m("div",B,[m("span",null,c(R.value.title)+" - 提交情况",1),d(y,{type:R.value.allowSubmit?"success":"danger",style:{"margin-left":"10px"}},{default:u((()=>[p(c(R.value.allowSubmit?"允许提交":"禁止提交"),1)])),_:1},8,["type"])]),m("div",null,[d(de,{modelValue:T.value,"onUpdate:modelValue":l[0]||(l[0]=e=>T.value=e),placeholder:"搜索用户",style:{width:"200px","margin-right":"10px"}},{prefix:u((()=>[d(oe,null,{default:u((()=>[d(b(g))])),_:1})])),_:1},8,["modelValue"]),d(re,{modelValue:D.value,"onUpdate:modelValue":l[1]||(l[1]=e=>D.value=e),placeholder:"状态筛选"},{default:u((()=>[d(ue,{label:"全部",value:""}),d(ue,{label:"已提交",value:"submitted"}),d(ue,{label:"未提交",value:"unsubmitted"})])),_:1},8,["modelValue"])])])])),default:u((()=>[d(ae,{data:P.value,style:{width:"100%"}},{default:u((()=>[d(i,{prop:"submittedBy",label:"用户",width:"120"},{default:u((({row:e})=>[p(c((e.submittedBy,e.submittedBy)),1)])),_:1}),d(i,{prop:"submitTime",label:"提交时间",width:"180"}),d(i,{prop:"originalFilename",label:"文件名",width:"180"},{default:u((({row:e})=>[p(c(e.originalFilename||("submitted"===e.status?"未知文件名":"-")),1)])),_:1}),d(i,{prop:"status",label:"状态",width:"120"},{default:u((({row:e})=>{return[d(y,{type:(l=e.status,{submitted:"success",unsubmitted:"info"}[l]||"info")},{default:u((()=>[p(c(N(e.status)),1)])),_:2},1032,["type"])];var l})),_:1}),d(i,{label:"操作",width:"180"},{default:u((({row:e})=>["submitted"===e.status?(h(),f(a,{key:0,type:"primary",link:"",onClick:l=>(async e=>{var l,a;try{let l=(await n.get(`/api/submissions/${e.id}`)).data.originalFilename||`作业-${e.id}.pdf`;l&&""!==l.trim()||(l=`${"string"==typeof e.submittedBy?e.submittedBy:""}-作业-${e.id}.pdf`);const a=s.info({message:"正在下载文件...",duration:0}),t=await fetch(`/api/submissions/${e.id}/file`,{headers:{Authorization:`Bearer ${localStorage.getItem("token")}`}});if(!t.ok)throw new Error(`下载失败: ${t.status} ${t.statusText}`);const i=await t.blob();a.close();const o=window.URL.createObjectURL(i),d=document.createElement("a");d.href=o,d.download=l,document.body.appendChild(d),d.click(),window.URL.revokeObjectURL(o),document.body.removeChild(d),s.success("文件下载成功")}catch(t){t.response,s.error((null==(a=null==(l=t.response)?void 0:l.data)?void 0:a.error)||t.message||"下载文件失败")}})(e)},{default:u((()=>l[18]||(l[18]=[p(" 查看作业 ")]))),_:2},1032,["onClick"])):v("",!0),d(a,{type:"warning",link:"",onClick:l=>{return a=e,M.value=a,O.value.content=a.feedback||"",void(L.value=!0);var a}},{default:u((()=>l[19]||(l[19]=[p(" 添加反馈 ")]))),_:2},1032,["onClick"])])),_:1})])),_:1},8,["data"]),m("div",U,[d(se,{column:4,border:""},{default:u((()=>[d(ne,{label:"总人数"},{default:u((()=>[p(c(A.length),1)])),_:1}),d(ne,{label:"已提交"},{default:u((()=>[p(c(G.value),1)])),_:1}),d(ne,{label:"未提交"},{default:u((()=>[p(c(J.value),1)])),_:1}),d(ne,{label:"提交率"},{default:u((()=>[p(c(K.value)+"%",1)])),_:1})])),_:1})])])),_:1})):v("",!0)])),_:1})])),_:1})])),_:1})])),_:1}),d(ye,{modelValue:Y.value,"onUpdate:modelValue":l[8]||(l[8]=e=>Y.value=e),title:"发布新作业",width:"500px"},{footer:u((()=>[m("span",I,[d(a,{onClick:l[7]||(l[7]=e=>Y.value=!1)},{default:u((()=>l[22]||(l[22]=[p("取消")]))),_:1}),d(a,{type:"primary",onClick:Z},{default:u((()=>l[23]||(l[23]=[p(" 发布 ")]))),_:1})])])),default:u((()=>[d(we,{model:E,rules:z,ref_key:"publishFormRef",ref:q},{default:u((()=>[d(fe,{label:"作业标题",prop:"title"},{default:u((()=>[d(de,{modelValue:E.title,"onUpdate:modelValue":l[2]||(l[2]=e=>E.title=e),placeholder:"请输入作业标题"},null,8,["modelValue"])])),_:1}),d(fe,{label:"作业说明",prop:"description"},{default:u((()=>[d(de,{modelValue:E.description,"onUpdate:modelValue":l[3]||(l[3]=e=>E.description=e),type:"textarea",rows:"4",placeholder:"请输入作业说明"},null,8,["modelValue"])])),_:1}),d(fe,{label:"截止日期",prop:"deadline"},{default:u((()=>[d(ve,{modelValue:E.deadline,"onUpdate:modelValue":l[4]||(l[4]=e=>E.deadline=e),type:"datetime",placeholder:"选择截止日期",format:"YYYY-MM-DD HH:mm","value-format":"YYYY-MM-DD HH:mm","disabled-date":X},null,8,["modelValue"])])),_:1}),d(fe,{label:"文件格式"},{default:u((()=>[d(ge,{modelValue:E.fileFormat,"onUpdate:modelValue":l[5]||(l[5]=e=>E.fileFormat=e)},{default:u((()=>[d(be,{label:"pdf"},{default:u((()=>l[20]||(l[20]=[p("仅PDF")]))),_:1}),d(be,{label:"any"},{default:u((()=>l[21]||(l[21]=[p("不限格式")]))),_:1})])),_:1},8,["modelValue"])])),_:1}),d(fe,{label:"提交控制"},{default:u((()=>[d(le,{modelValue:E.allowSubmit,"onUpdate:modelValue":l[6]||(l[6]=e=>E.allowSubmit=e),"active-text":"允许提交","inactive-text":"禁止提交"},null,8,["modelValue"])])),_:1})])),_:1},8,["model"])])),_:1},8,["modelValue"]),d(ye,{modelValue:L.value,"onUpdate:modelValue":l[11]||(l[11]=e=>L.value=e),title:"添加反馈",width:"500px"},{footer:u((()=>[m("span",$,[d(a,{onClick:l[10]||(l[10]=e=>L.value=!1)},{default:u((()=>l[24]||(l[24]=[p("取消")]))),_:1}),d(a,{type:"primary",onClick:ee},{default:u((()=>l[25]||(l[25]=[p(" 确认 ")]))),_:1})])])),default:u((()=>[d(we,{model:O.value},{default:u((()=>[d(fe,{label:"反馈内容","label-width":"100px"},{default:u((()=>[d(de,{modelValue:O.value.content,"onUpdate:modelValue":l[9]||(l[9]=e=>O.value.content=e),type:"textarea",rows:"4",placeholder:"请输入反馈内容"},null,8,["modelValue"])])),_:1})])),_:1},8,["model"])])),_:1},8,["modelValue"])])}}}),[["__scopeId","data-v-eba6035a"]]);export{F as default};

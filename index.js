import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
const { PhotoPluginModule, PhotoPlugin } = NativeModules;
const DEFAULT_QUALITY = 50;
const COMPRESS_LIMIT = 25;
const DEFAULT_MIN_VIDEO_TIME = 5;
const emitter = new NativeEventEmitter(PhotoPlugin);
const EventName = 'InProgress';
const listeners = new Set();
emitter.addListener(EventName, (...args) => {
    Array.from(listeners).forEach((cb) => {
        cb(...args);
    });
});
export default class RNPhotoPlugin {
    /**
     * android接口，请尽量不要直接使用此方法
     * @param action 操作
     * @param args 参数
     */
    static execute(action, ...args) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(action, ...args);
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 选相册图片 （图片）
     * @param maxCount 最大数量
     * @param width 宽
     * @param quality 质量, 范围1~100
     * @param minCount 最小数量
     */
    static selectImage(maxCount, width, quality, minCount = 0) {
        const args = [maxCount, width, quality, minCount];

        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_SELECT_IMAGE, args);
        }
        else if (Platform.OS === 'ios') {
            return PhotoPlugin.selectAlbum(maxCount, width, quality, minCount)
                .then((result) => {
                    return result.image_paths;
                });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 验证的录视频 （视频）
     * @param text 显示的文本
     * @param quality 质量, 范围1~100
     */
    static recordVideo(text, quality = DEFAULT_QUALITY) {
        let p;
        if (Platform.OS === 'android') {
            p = PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_TAKE_VIDEO, [text, DEFAULT_QUALITY]).then((result) => {
                return [result];
            });
        }
        else if (Platform.OS === 'ios') {
            PhotoPlugin.videoSizeLimit(COMPRESS_LIMIT);
            p = PhotoPlugin.recordVideo(text, DEFAULT_QUALITY)
                .then((result) => {
                    return result.videos;
                });
        }
        else {
            p = Promise.reject('platfrom not support');
        }
        return p.then(path => path ? path : Promise.reject('cancel'));
    }
    /**
     * 选头像 （图片）
     * @param width 宽
     * @param quantity 质量, 范围0 ~ 100
     */
    static selectAvatar(width, quantity) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_SELECT_AVATAR, [width, quantity]);
        }
        else if (Platform.OS === 'ios') {
            return PhotoPlugin.selectAvatar(width, quantity);
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 选视频 （视频）
     * @param duration 时长，单位为秒
     * @param quantity 质量, 范围0 ~ 100
     * customCompress?: boolean, // 是否用自定义的压缩方式
     * vq_bitRate?: number // 比特率
     * vq_width?: number //视频宽度
     * vq_height?: number // 视频高度
     */
    static selectAlbumVideo({ maxDuration, videoQuality = DEFAULT_QUALITY, customCompress = true, vq_bitRate, vq_width, vq_height, minDuration = DEFAULT_MIN_VIDEO_TIME, }) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_SELECT_ALBUM_VIDEO, [maxDuration, videoQuality]);
        }
        else if (Platform.OS === 'ios') {
            PhotoPlugin.videoSizeLimit(COMPRESS_LIMIT);
            // return PhotoPlugin.selectAlbumVideo(maxDuration, DEFAULT_QUALITY)
            //   .then((result: { image_paths: string[], videos: string[] }) => {
            //     return result.videos
            //   })
            return PhotoPlugin.selectAlbumVideoOption({ minDuration, maxDuration, videoQuality, customCompress, vq_bitRate, vq_width, vq_height })
                .then((result) => {
                    return result.videos;
                });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 选不压缩视频 （视频）
     * @param {number} duration
     * @returns {Promise<any>}
     */
    static selectUncompressedVideo(duration) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.selectUncompressedVideo(duration);
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 现场拍照 （图片）
     * @returns {Promise<string>} 照片路径
     * @param quantity 质量, 范围0 ~ 100
     * @param isFront, 打开摄像头是前置 or 后置
     */
    static takePhoto(width, quantity, isFront = false) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_TAKE_PHOTO, [width, quantity, isFront]);
        }
        else if (Platform.OS === 'ios') {
            return PhotoPlugin.takePhoto(quantity, isFront)
                .then((result) => {
                    return result.image_paths[0].replace(/^file:\/\//, '');
                });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 现场录像 (视频)
     * @param {number} minDuration 最小时长，单位为秒
     * @param {number} maxDuration 最大时长，单位为秒
     * @param quantity 质量, 范围0 ~ 100
     * @param isFront, 打开摄像头是前置 or 后置
     * @returns {any}
     */
    static takeVideo(minDuration, maxDuration, quantity = DEFAULT_QUALITY, isFront = false) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_CAPTURE_VIDEO, [maxDuration, DEFAULT_QUALITY, isFront]);
        }
        else if (Platform.OS === 'ios') {
            PhotoPlugin.videoSizeLimit(COMPRESS_LIMIT);
            return PhotoPlugin.recordVideo2(minDuration, maxDuration, DEFAULT_QUALITY, isFront)
                .then((result) => {
                    return result.videos;
                });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 选视频&照片 (图片，视频)
     * @param {number} minCount 最小数量
     * @param {number} maxCount 最大数量
     * @param {number} width 宽
     * @param {number} picQuality 质量 0 - 100
     * @param {number} videoQuality 质量 0 - 100
     * @param {number} duration 时长，单位为秒
     * @param {number} gifSizeLimit 的大小限制,单位M
     * @returns {Promise<any>}
     */
    static selectAlbumPhotoAndVideo(maxCount, width, picQuality, videoQuality = DEFAULT_QUALITY, maxDuration, gifSizeLimit, minCount = 0) {
        if (Platform.OS === 'android') {
            PhotoPluginModule.gifSizeLimit(gifSizeLimit);
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_SELECT_ALBUM_IMAGE_VIDEO, [maxCount, false, maxDuration, width, picQuality, videoQuality, minCount])
                .then((result) => {
                    let tmpResult = result;
                    tmpResult.videos = tmpResult.videos[0];
                    return tmpResult;
                });
        }
        else if (Platform.OS === 'ios') {
            PhotoPlugin.videoSizeLimit(COMPRESS_LIMIT);
            PhotoPlugin.gifSizeLimit(gifSizeLimit);
            return PhotoPlugin.selectAlbumPhotoAndVideo(maxCount, width, picQuality, DEFAULT_QUALITY, maxDuration, minCount);
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * IOS only
     * maxCount,  // 选图片最大张数
     * width,         // 图片宽
     * picQuality,  // 图片压缩质量
     * videoQuality, //原来的压缩质量
     * maxDuration,  // 视频最大长度
     * gifSizeLimit,  // gif大小限制
     * customCompress?: boolean, // 是否用自定义的压缩方式
     * vid?: string, // 异步压缩的id  （传了这个参数说明是异步压缩，不传就是同步）
     * vq_bitRate?: number // 比特率
     * vq_width?: number //视频宽度
     * vq_height?: number // 视频高度
     * @returns {Promise<any>}
     */
    static IOS_selectAlbumPhotoAndVideoCanAsync({ maxCount, width, picQuality, videoQuality = DEFAULT_QUALITY, maxDuration, gifSizeLimit, customCompress = true, vid, vq_bitRate, vq_width, vq_height }) {
        if (Platform.OS === 'ios') {
            // PhotoPlugin.videoSizeLimit(COMPRESS_LIMIT);
            PhotoPlugin.gifSizeLimit(gifSizeLimit);
            return PhotoPlugin.selectAlbumPhotoAndVideoCanAsync({
                maxCount, width, picQuality, videoQuality,
                maxDuration, gifSizeLimit, customCompress, vid, vq_bitRate, vq_width, vq_height
            });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 选视频&照片 (图片，视频，GIF) 视频未压缩
     * @param {number} maxCount 最大数量
     * @param {number} width 宽
     * @param {number} quality 质量 0 - 100
     * @param {number} duration 时长，单位为秒
     * @param {number} gifSizeLimit 的大小限制,单位M
     * @param {number} minCount 最小数量
     * @returns {Promise<any>}
     */
    static selectAlbumAndUncompressedVideo(maxCount, width, quality, maxDuration, gifSizeLimit, minCount = 0) {
        if (Platform.OS === 'android') {
            PhotoPluginModule.gifSizeLimit(gifSizeLimit);
            return PhotoPluginModule.selectAlbumAndUncompressedVideo(minCount, maxCount, width, quality, maxDuration).then((result) => {
                let tmpResult = result;
                tmpResult.videos = tmpResult.videos[0];
                return tmpResult;
            });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 上传表情（图片+GIF）
     * @param {number} count 数量
     * @param {number} width 宽
     * @param {number} quality 质量 0 - 100
     * @param {number} gifSizeLimit 的大小限制,单位M
     */
    static selectGifAndPic(count, width, quality, gifSizeLimit) {
        if (Platform.OS === 'android') {
            PhotoPluginModule.gifSizeLimit(gifSizeLimit);
            return PhotoPluginModule.selectGifAndPic(count, width, quality);
        }
        else if (Platform.OS === 'ios') {
            PhotoPlugin.gifSizeLimit(gifSizeLimit);
            return PhotoPlugin.selectGifAndPic(count, width, quality)
                .then((result) => {
                    return result.image_paths;
                });
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 压缩视频
     * @param path 视频路径
     * @param {number} quality 质量 0 - 100
     * @return (Promise<any>) 返回一个map对象 { 'original':(原始路径),'result':(压缩后路径)}
     */
    static compressVideo(path, maxDuration, quality = DEFAULT_QUALITY) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.compressVideo(path, maxDuration, DEFAULT_QUALITY);
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    /**
     * 设置插件语言
     * @param {'zh'|'en'} lang 语言, 'zh' 或 'en'
     */
    static setLang(lang) {
        if (Platform.OS === 'android') {
            return PhotoPluginModule.execute(RNPhotoPlugin.Constants.ACTION_CHANGE_LANGUAGE, [lang]);
        }
        else if (Platform.OS === 'ios') {
            return PhotoPlugin.changeLanguage(lang);
        }
        else {
            return Promise.reject('platfrom not support');
        }
    }
    static onProgress(cb) {
        listeners.clear();
        listeners.add(cb);
    }
    static initStringConfig(config, defaultLanguage = 'zh') {
        if (Platform.OS === 'android') {
            PhotoPluginModule.initStringConfig(config, defaultLanguage);
        }
        else {
            PhotoPlugin.setLangSrc(config);
        }
    }
    static initImageConfig(config, defaultLanguage = 'zh') {
        if (Platform.OS === 'android') {
            PhotoPluginModule.initImageConfig(config, defaultLanguage);
        }
        else {
            PhotoPlugin.setImageSrc(config);
        }
    }
    static initColorConfig(config, defaultLanguage = 'zh') {
        if (Platform.OS === 'android') {
            PhotoPluginModule.initColorConfig(config, defaultLanguage);
        }
        else {
            PhotoPlugin.setCss(config);
        }
    }
    /**
     * iOS改变状态栏颜色：传入true，状态栏颜色变为白色，传入false，状态栏颜色变为黑色。
     */
    static setStatusBarLight(isLight) {
        PhotoPlugin.setStatusBarLight(isLight);
    }
}
RNPhotoPlugin.Constants = {
    ACTION_SELECT_IMAGE: 'selectAlbum',
    ACTION_TAKE_VIDEO: 'recordVideo',
    ACTION_SELECT_AVATAR: 'selectAvatar',
    ACTION_SELECT_ALBUM_VIDEO: 'selectAlbumVideo',
    ACTION_SELECT_ALBUM_IMAGE_VIDEO: 'selectImageVideo',
    ACTION_TAKE_PHOTO: 'take_photo',
    ACTION_CAPTURE_VIDEO: 'captureVideo',
    ACTION_CHANGE_LANGUAGE: 'change_language'
};

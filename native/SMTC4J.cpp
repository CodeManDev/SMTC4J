#include <jni.h>
#include <winrt/Windows.Media.Control.h>
#include <winrt/Windows.Storage.Streams.h>
#include <winrt/Windows.Foundation.h>
#include <string>
#include <vector>
#include <chrono>

using namespace winrt;
using namespace Windows::Media::Control;
using namespace Windows::Storage::Streams;

static std::string base64Encode(const std::vector<uint8_t>& data) {
    static auto chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    std::string out;
    int val = 0, valb = -6;
    for (uint8_t c : data) {
        val = (val << 8) + c;
        valb += 8;
        while (valb >= 0) {
            out.push_back(chars[(val >> valb) & 0x3F]);
            valb -= 6;
        }
    }
    if (valb > -6)
        out.push_back(chars[((val << 8) >> (valb + 8)) & 0x3F]);
    while (out.size() % 4) out.push_back('=');
    return out;
}

static std::string getThumbnailBase64(GlobalSystemMediaTransportControlsSessionMediaProperties const& mediaProps) {
    try {
        const auto thumbnail = mediaProps.Thumbnail();
        if (!thumbnail) return "";

        const auto stream = thumbnail.OpenReadAsync().get();
        if (!stream) return "";

        const auto size = stream.Size();
        if (size == 0) return "";

        std::vector<uint8_t> buffer(size);
        const DataReader reader(stream);
        reader.LoadAsync(static_cast<uint32_t>(size)).get();
        reader.ReadBytes(buffer);

        return base64Encode(buffer);
    } catch (...) {
        return "";
    }
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_dev_codeman_smtc4j_SMTC4J_getPlaybackState(JNIEnv* env, jclass) {
    try {
        try {
            winrt::init_apartment();
        } catch (const winrt::hresult_invalid_argument&) {
        }

        const auto manager = GlobalSystemMediaTransportControlsSessionManager::RequestAsync().get();
        const auto session = manager.GetCurrentSession();
        if (!session) return env->NewStringUTF("{}");

        const auto info = session.GetPlaybackInfo();
        const auto status = info.PlaybackStatus();

        int stateCode = 0;
        switch (status) {
            case GlobalSystemMediaTransportControlsSessionPlaybackStatus::Playing: stateCode = 2; break;
            case GlobalSystemMediaTransportControlsSessionPlaybackStatus::Paused:  stateCode = 1; break;
            case GlobalSystemMediaTransportControlsSessionPlaybackStatus::Stopped: stateCode = 0; break;
            default: stateCode = -1; break;
        }

        const auto timeline = session.GetTimelineProperties();
        double positionSec = timeline.Position().count() / 10'000'000.0; // to seconds

        if (stateCode == 2) { // update position based on elapsed time while playing
            const auto lastUpdated = timeline.LastUpdatedTime().time_since_epoch();
            const auto now = winrt::clock::now().time_since_epoch();
            const auto deltaTicks = now.count() - lastUpdated.count();
            positionSec += deltaTicks / 10'000'000.0; // to seconds
        }

        const std::string json = "{"
            "\"stateCode\":" + std::to_string(stateCode) + ","
            "\"position\":" + std::to_string(positionSec) +
        "}";

        return env->NewStringUTF(json.c_str());

    } catch (const winrt::hresult_error& e) {
        std::string err = std::string(R"({"error":")") + winrt::to_string(e.message()) + "\"}";
        return env->NewStringUTF(err.c_str());
    } catch (...) {
        return env->NewStringUTF(R"({"error":"unknown exception"})");
    }
}

JNIEXPORT jstring JNICALL
Java_dev_codeman_smtc4j_SMTC4J_getMediaInfo(JNIEnv* env, jclass) {
    try {
        try {
            winrt::init_apartment();
        } catch (const winrt::hresult_invalid_argument&) {
        }

        const auto manager = GlobalSystemMediaTransportControlsSessionManager::RequestAsync().get();
        const auto session = manager.GetCurrentSession();
        if (!session) return env->NewStringUTF("{}");

        const auto mediaProps = session.TryGetMediaPropertiesAsync().get();
        const auto timeline = session.GetTimelineProperties();

        const double durationSec = timeline.EndTime().count() / 10'000'000.0;
        const std::string thumbnailBase64 = getThumbnailBase64(mediaProps);
        const auto sourceApp = winrt::to_string(session.SourceAppUserModelId());

        const std::string json = "{"
            "\"title\":\"" + winrt::to_string(mediaProps.Title()) + "\","
            "\"artist\":\"" + winrt::to_string(mediaProps.Artist()) + "\","
            "\"album\":\"" + winrt::to_string(mediaProps.AlbumTitle()) + "\","
            "\"duration\":" + std::to_string(durationSec) + ","
            "\"sourceApp\":\"" + sourceApp + "\","
            "\"thumbnailBase64\":\"" + thumbnailBase64 + "\""
        "}";

        return env->NewStringUTF(json.c_str());

    } catch (const winrt::hresult_error& e) {
        const std::string err = std::string(R"({"error":")") + winrt::to_string(e.message()) + "\"}";
        return env->NewStringUTF(err.c_str());
    } catch (...) {
        return env->NewStringUTF(R"({"error":"unknown exception"})");
    }
}

}
